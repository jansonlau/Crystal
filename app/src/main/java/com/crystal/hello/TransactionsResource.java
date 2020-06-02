//package com.crystal.hello;
//
//import android.content.Context;
//import android.widget.ArrayAdapter;
//import android.widget.ListView;
//
//import com.plaid.client.request.TransactionsGetRequest;
//import com.plaid.client.response.TransactionsGetResponse;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Date;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;

//                                TransactionsResource transactionsResource = new TransactionsResource(HomeActivity.this);
//                                transactionsResource.getTransactions();
//
//public class TransactionsResource extends HomeActivity {
//    Context context;
//    public TransactionsResource(Context homeActivity) {
//        super();
//        context = homeActivity;
//    }
//
//    public void getTransactions() {
//        /** Code for getting categories https://plaid.com/docs/#categories
//         plaidClient.service().categoriesGet(new CategoriesGetRequest()).enqueue(new Callback<CategoriesGetResponse>() {
//        @Override
//        public void onResponse(Call<CategoriesGetResponse> call, Response<CategoriesGetResponse> response) {
//        List<CategoriesGetResponse.Category> categories = response.body().getCategories();
//        }
//
//        @Override
//        public void onFailure(Call<CategoriesGetResponse> call, Throwable t) {
//
//        }
//        });
//         */
//
//        Date startDate = new Date(System.currentTimeMillis() - 86400000L * 100);
//        Date endDate = new Date();
//
//        TransactionsGetRequest request =
//                new TransactionsGetRequest(accessToken, startDate, endDate)
//                        .withCount(100);
//
//        plaidClient.service().transactionsGet(request).enqueue(new Callback<TransactionsGetResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<TransactionsGetResponse> call,
//                                   @NotNull Response<TransactionsGetResponse> response) {
//                if (response.isSuccessful()) {
//                    if (response.body() != null) {
////                        for (TransactionsGetResponse.Transaction transaction : response.body().getTransactions()) {
////                            Log.d("Transactions", transaction.getName());
////                        }
//                        final ListView listNotes = findViewById(R.id.list);
//                        ArrayAdapter<TransactionsGetResponse.Transaction> arrayAdapter = new ArrayAdapter<>(TransactionsResource.this,
//                                android.R.layout.simple_list_item_1, response.body().getTransactions());
//                        listNotes.setAdapter(arrayAdapter);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<TransactionsGetResponse> call, @NotNull Throwable t) {
//
//            }
//        });
//    }
//}
