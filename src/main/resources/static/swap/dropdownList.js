let fromSymbol = '';
let fromQuantity = 0;
let fromQuantityString = '0';
let fromPrice = 0;

let toSymbol = '';
let toPrice = 0;

function setupDropdown(containerSelector) {
    const container = document.querySelector(containerSelector);
    if (!container) return;

    const searchInput = container.querySelector('.swap__input');
    const hiddenInput = container.querySelector('input[type="hidden"]');
    const dropdownList = container.querySelector('.dropdown-list');

    searchInput.addEventListener('click', () => {
        dropdownList.style.display = 'block';
    });

    searchInput.addEventListener('input', () => {
        const filter = searchInput.value.toLowerCase();
        const items = dropdownList.querySelectorAll('.dropdown-item');
        items.forEach(item => {
            const text = item.innerText.toLowerCase();
            item.style.display = text.includes(filter) ? '' : 'none';
        });
    });

    dropdownList.addEventListener('click', (event) => {
        const selectedItem = event.target.closest('.dropdown-item');
        if (!selectedItem) return;

        const { name, symbol, quantity } = selectedItem.dataset;

        searchInput.value = `${name} (${symbol})`;
        hiddenInput.value = symbol;
        dropdownList.style.display = 'none';

        const priceSpan = selectedItem.querySelector('.item-price');
        let currentPrice = 0;
        if (priceSpan) {
            const priceMatch = priceSpan.textContent.match(/[\d,.]+/);
            if (priceMatch) currentPrice = parseFloat(priceMatch[0].replace(/,/g, ''));
        }

        if (container.classList.contains('swap__from')) {
            fromSymbol = symbol;
            fromQuantity = parseFloat(quantity);
            fromQuantityString = quantity;
            fromPrice = currentPrice;

            document.getElementById('quantitySection').style.display = 'block';
            document.getElementById('maxFromQuantity').textContent = quantity;
            updatePriceDisplay('currentPrice', fromPrice, fromSymbol);
        } else {
            toSymbol = symbol;
            toPrice = currentPrice;

            document.querySelector('.to-price__display').style.display = 'block';
            updatePriceDisplay('currentToPrice', toPrice, toSymbol);
        }
    });
}

function updatePriceDisplay(elementId, price, symbol) {
    const priceElement = document.getElementById(elementId);
    if (priceElement && price > 0 && symbol)
        priceElement.textContent = `${price.toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 8
        })} per ${symbol}`;
}

function refreshFromPrice() {
    if (!fromSymbol) return;

    const dropdownList = document.querySelector('.swap__from .dropdown-list');
    const selectedItem = dropdownList?.querySelector(`[data-symbol="${fromSymbol}"]`);
    if (!selectedItem) return;

    const priceSpan = selectedItem.querySelector('.item-price');
    if (priceSpan) {
        const priceMatch = priceSpan.textContent.match(/[\d,.]+/);
        if (priceMatch) fromPrice = parseFloat(priceMatch[0].replace(/,/g, ''));
    }

    const quantity = selectedItem.dataset.quantity;
    fromQuantity = parseFloat(quantity);
    fromQuantityString = quantity;
    document.getElementById('maxFromQuantity').textContent = quantity;
    updatePriceDisplay('currentPrice', fromPrice, fromSymbol);
}

function refreshToPrice() {
    if (!toSymbol) return;

    const dropdownListTo = document.querySelector('.swap__to .dropdown-list');
    const selectedItem = dropdownListTo?.querySelector(`[data-symbol="${toSymbol}"]`);
    if (!selectedItem) return;

    const priceSpan = selectedItem.querySelector('.item-price');
    if (priceSpan) {
        const priceMatch = priceSpan.textContent.match(/[\d,.]+/);
        if (priceMatch) toPrice = parseFloat(priceMatch[0].replace(/,/g, ''));
    }
    updatePriceDisplay('currentToPrice', toPrice, toSymbol);
}

setupDropdown('.swap__from');
setupDropdown('.swap__to');

document.querySelector('.quantity__buttons').addEventListener('click', (event) => {
    if (event.target.classList.contains('btn-percent')) {
        const percent = parseFloat(event.target.dataset.percent);
        const quantityInput = document.getElementById('quantityInput');

        if (percent === 1) quantityInput.value = fromQuantityString;
        else if (fromQuantity > 0) quantityInput.value = (fromQuantity * percent).toFixed(8);
    }
});

document.addEventListener('click', (event) => {
    const fromDropdown = document.querySelector('.swap__from .dropdown-list');
    const toDropdown = document.querySelector('.swap__to .dropdown-list');

    if (!event.target.closest('.swap__from')) fromDropdown.style.display = 'none';
    if (!event.target.closest('.swap__to')) toDropdown.style.display = 'none';
});

document.addEventListener('htmx:afterSwap', function(event) {
    const targetId = event.detail.target.id;
    if (targetId === 'dropdownList') refreshFromPrice();
    if (targetId === 'dropdownListTo') refreshToPrice();
});