const stripe = Stripe('pk_test_51R6mqdIc44qIhXiAT7yJAOJWWhFdxHgTTV30JpnawCX63jE6yEtM9Cdh65AbYB0wDoqSPcHM4xl39AyeZljNmGI100x9QgwDbl');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
 stripe.redirectToCheckout({
   sessionId: sessionId
 })
});