package com.crystal.hello.ui.home;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crystal.hello.monthlyactivity.MonthlyActivityViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.Account;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.TransactionsGetResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {
    // Key Initialization vector, Value encrypted data
    private Map<byte[], byte[]> encryptedAccessTokenMap;
    private Map<byte[], byte[]> encryptedItemIdMap;

    private final String clientIdKey          = "5e9e830fd1ed690012c3be3c";
    private final String developmentSecretKey = "60accf9202c1cb270909846affe85a";
    private final String sandboxSecretKey     = "74cf176067e0712cc2eabdf800829e";
    private static final String PROVIDER = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private MutableLiveData<Double> currentTotalBalance;
    private MutableLiveData<List<DocumentSnapshot>> mutableSubsetTransactionsList;
    private static List<Map<String, Object>> bankAccountsList;
    private Map<String, Account> accountIdToAccountMap;
    private static MonthlyActivityViewModel monthlyActivityViewModel;

    private PlaidClient plaidClient;
    private int transactionOffset;
    private final FirebaseFirestore db;
    private final DocumentReference docRef;


    public HomeViewModel() {
        currentTotalBalance             = new MutableLiveData<>();
        mutableSubsetTransactionsList   = new MutableLiveData<>();
        accountIdToAccountMap           = new HashMap<>(); // Credit card accounts only
        transactionOffset               = 0;
        db                              = FirebaseFirestore.getInstance();
        docRef                          = db.collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        monthlyActivityViewModel        = new MonthlyActivityViewModel();
    }

    public LiveData<Double> getCurrentTotalBalance() {
        return currentTotalBalance;
    }

    public LiveData<List<DocumentSnapshot>> getMutableSubsetTransactionsList() {
        return mutableSubsetTransactionsList;
    }

    public List<Map<String, Object>> getBankAccountsList() {
        return bankAccountsList;
    }

    public static MonthlyActivityViewModel getMonthlyActivityViewModel() {
        return monthlyActivityViewModel;
    }

    protected void buildPlaidClient() {
        plaidClient = PlaidClient.newBuilder()
                .clientIdAndSecret(clientIdKey, developmentSecretKey)
                .developmentBaseUrl()
                .build();
    }

    // Asynchronously get access token for a bank account
    protected void exchangeAccessToken(String publicToken) {
        plaidClient.service()
                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
                .enqueue(new Callback<ItemPublicTokenExchangeResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                           @NotNull Response<ItemPublicTokenExchangeResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ItemPublicTokenExchangeResponse responseBody = response.body();
                            try {
                                encryptedAccessTokenMap = encrypt("accessToken", responseBody.getAccessToken());
                                encryptedItemIdMap = encrypt("itemId", responseBody.getItemId());
                            } catch (NoSuchProviderException
                                    | NoSuchAlgorithmException
                                    | InvalidAlgorithmParameterException
                                    | NoSuchPaddingException
                                    | InvalidKeyException
                                    | BadPaddingException
                                    | IllegalBlockSizeException e) {
                                e.printStackTrace();
                            }

                            getPlaidAccountsAndTransactions(transactionOffset);
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ItemPublicTokenExchangeResponse> call,
                                          @NotNull Throwable t) {
                    }
                });
    }

    private Map<byte[], byte[]> encrypt(String alias, String data) throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            NoSuchPaddingException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER);
        keyGenerator.init(new KeyGenParameterSpec
                .Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        final SecretKey secretKey = keyGenerator.generateKey();
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] initializationVector = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        Map<byte[], byte[]> IVAndDataMap = new HashMap<>();
        IVAndDataMap.put(initializationVector, encryptedData);
        return IVAndDataMap;
    }

    private String decrypt(String alias, byte[] initializationVector, byte[] encryptedData) throws KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            UnrecoverableEntryException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            NoSuchPaddingException,
            BadPaddingException,
            IllegalBlockSizeException {
        KeyStore keyStore = KeyStore.getInstance(PROVIDER);
        keyStore.load(null);

        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
        final SecretKey secretKey = secretKeyEntry.getSecretKey();
        final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        final GCMParameterSpec spec = new GCMParameterSpec(128, initializationVector);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        final byte[] decodedData = cipher.doFinal(encryptedData);
        return new String(decodedData, StandardCharsets.UTF_8);
    }

    // TODO: Plaid Liabilities for upcoming bill amount
//    private void getPlaidLiabilities() {
//        LiabilitiesGetRequest request = new LiabilitiesGetRequest(accessToken);
//        plaidClient.service().liabilitiesGet(request).enqueue(new Callback<LiabilitiesGetResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<LiabilitiesGetResponse> call,
//                                   @NotNull Response<LiabilitiesGetResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<LiabilitiesGetResponse.CreditCardLiability> creditCardList = response.body().getLiabilities().getCredit();
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<LiabilitiesGetResponse> call,
//                                  @NotNull Throwable t) {
//
//            }
//        });
//    }

    // Plaid Transactions for Accounts and Transactions
    private void getPlaidAccountsAndTransactions(Integer offset) {
        final int count = 500;
//        Date startDate = new Date(1511049600L); // 1970
        Date startDate = new Date(System.currentTimeMillis() - 1511049600L * 100); // 2017
//        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100); // 2020
        Date endDate = new Date();

        String accessToken = null;
        for (Map.Entry<byte[], byte[]> entry : encryptedAccessTokenMap.entrySet()) {
            try {
                accessToken = decrypt("accessToken", entry.getKey(), entry.getValue());
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException
                    | IOException
                    | UnrecoverableEntryException
                    | InvalidAlgorithmParameterException
                    | InvalidKeyException
                    | NoSuchPaddingException
                    | BadPaddingException
                    | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        TransactionsGetRequest request = new TransactionsGetRequest(Objects.requireNonNull(accessToken), startDate, endDate)
                .withCount(count)
                .withOffset(offset);

        plaidClient.service()
                .transactionsGet(request)
                .enqueue(new Callback<TransactionsGetResponse>() {
            @Override
            public void onResponse(@NotNull Call<TransactionsGetResponse> call,
                                   @NotNull Response<TransactionsGetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TransactionsGetResponse responseBody = response.body();

                    // Get credit card accounts once
                    // Accounts include account name and current balance
                    if (transactionOffset == 0) {
                        for (Account account : responseBody.getAccounts()) {
                            if (account.getSubtype().equals("credit card")) {
                                accountIdToAccountMap.put(account.getAccountId(), account);
                            }
                        }
                        setPlaidAccountsToDatabase();
                    }

                    // Get transactions
                    List<TransactionsGetResponse.Transaction> paginatedTransactionsList = new ArrayList<>();
                    for (String accountId : accountIdToAccountMap.keySet()) {
                        for (TransactionsGetResponse.Transaction transaction : responseBody.getTransactions()) {
                            if (transaction.getAccountId().equals(accountId)) {
                                paginatedTransactionsList.add(transaction);
                            }
                        }
                    }

                    transactionOffset += count;
                    int totalTransactions = responseBody.getTotalTransactions();
                    setPaginatedPlaidTransactionsToDatabase(paginatedTransactionsList, totalTransactions);
                }
            }

            @Override
            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
            }
        });
    }

    // Write to Firestore with paginated list because of Plaid's 500 transaction limit and
    // Firestore's WriteBatch has limit of 500 documents
    // Set Plaid Transaction to Firestore's "transactions" collection with Plaid transactionId as document ID
    private void setPaginatedPlaidTransactionsToDatabase(List<TransactionsGetResponse.Transaction> paginatedTransactionsList,
                                                         int totalTransactions) {
        final WriteBatch batch = db.batch();
        for (TransactionsGetResponse.Transaction transaction : paginatedTransactionsList) {
            DocumentReference transactionsRef = docRef.collection("transactions")
                    .document(transaction.getTransactionId());

            batch.set(transactionsRef, transaction, SetOptions.merge());
        }

        // If there are more than 500 transactions, get more because they're paginated
        if (transactionOffset < totalTransactions) {
            getPlaidAccountsAndTransactions(transactionOffset); // Get all transactions within the date period set
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (transactionOffset >= totalTransactions) {
                            getSubsetTransactionsFromDatabase();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    // Set Plaid Account to "banks" collection with Plaid accountId as document ID
    private void setPlaidAccountsToDatabase() {
        String accessToken = null;
        for (Map.Entry<byte[], byte[]> entry : encryptedAccessTokenMap.entrySet()) {
            try {
                accessToken = decrypt("accessToken", entry.getKey(), entry.getValue());
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException
                    | IOException
                    | UnrecoverableEntryException
                    | InvalidAlgorithmParameterException
                    | InvalidKeyException
                    | NoSuchPaddingException
                    | BadPaddingException
                    | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        String itemId = null;
        for (Map.Entry<byte[], byte[]> entry : encryptedItemIdMap.entrySet()) {
            try {
                itemId = decrypt("itemId", entry.getKey(), entry.getValue());
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException
                    | IOException
                    | UnrecoverableEntryException
                    | InvalidAlgorithmParameterException
                    | InvalidKeyException
                    | NoSuchPaddingException
                    | BadPaddingException
                    | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        final WriteBatch batch = db.batch();
        for (Account account : accountIdToAccountMap.values()) {
            Map<String, Object> identifiers = new HashMap<>();
            identifiers.put("accessToken", accessToken);
            identifiers.put("itemId", itemId);

            DocumentReference identifiersRef = docRef.collection("identifiers")
                    .document(account.getAccountId());

            DocumentReference banksRef = docRef.collection("banks")
                    .document(account.getAccountId());

            batch.set(identifiersRef, identifiers, SetOptions.merge());
            batch.set(banksRef, account, SetOptions.merge());
        }

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        getBalancesAndBankAccountsFromDatabase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    protected void getSubsetTransactionsFromDatabase() {
        docRef.collection("transactions")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final List<DocumentSnapshot> subsetTransactionsList = Objects.requireNonNull(task.getResult()).getDocuments();
                            mutableSubsetTransactionsList.setValue(subsetTransactionsList);
                        }
                    }
                });
    }

    protected void getBalancesAndBankAccountsFromDatabase() {
        docRef.collection("banks")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            double totalBalance = 0;
                            Map<String, Object> bankAccountMap;
                            Map<String, Object> balances;
                            bankAccountsList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                bankAccountMap = document.getData();
                                bankAccountsList.add(bankAccountMap);
                                balances = (HashMap<String, Object>) bankAccountMap.get("balances");
                                totalBalance += (double) Objects.requireNonNull(balances).get("current");
                            }
                            currentTotalBalance.setValue(totalBalance);
                        }
                    }
                });
    }
}