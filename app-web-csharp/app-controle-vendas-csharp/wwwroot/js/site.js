// Mostra o campo de valor pago apenas quando a forma de pagamento é Dinheiro.
(function () {
    var radios = document.querySelectorAll('input[name="Input.Method"]');
    var cashOnly = document.querySelectorAll('[data-cash-only]');
    if (!radios.length || !cashOnly.length) return;

    function update() {
        var cash = document.querySelector('input[name="Input.Method"][value="Cash"]');
        cashOnly.forEach(function (el) { el.classList.toggle('hidden', !(cash && cash.checked)); });
    }

    radios.forEach(function (r) { r.addEventListener('change', update); });
    update();
})();
