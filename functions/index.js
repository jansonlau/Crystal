const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

const plaid = require('plaid');

const plaidClient = new plaid.Client({
	clientID: '5e9e830fd1ed690012c3be3c',
	secret: '60accf9202c1cb270909846affe85a',
	env: plaid.environments.development,
});

exports.getLinkToken = functions.https.onCall(async (data) => {
	const clientUserId = data.userId;
	const linkTokenResponse = await plaidClient.createLinkToken({
		user: {
			  client_user_id: clientUserId,
		},
		client_name: 'Crystal',
		products: ['transactions'],
		country_codes: ['US'],
		language: 'en',
		android_package_name: 'com.crystal.hello',
		// webhook: 'https://sample.webhook.com',
		account_filters: {
			credit: {
				account_subtypes: ['credit card'],
			},
		},
	});

  	const link_token = linkTokenResponse.link_token;
  	return { linkToken: link_token };
});