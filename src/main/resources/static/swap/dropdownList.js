let currentMaxQuantity = 0;
let currentMaxQuantityString = '0'; // ✨ ADDED: Stores the exact string value

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

        if (container.classList.contains('swap__from')) {
            document.getElementById('quantitySection').style.display = 'block';
            currentMaxQuantity = parseFloat(quantity);
            currentMaxQuantityString = quantity;
            document.getElementById('maxFromQuantity').textContent = quantity;
        }
    });
}

setupDropdown('.swap__from');
setupDropdown('.swap__to');

document.querySelector('.quantity__buttons').addEventListener('click', (event) => {
    if (event.target.classList.contains('btn-percent')) {
        const percent = parseFloat(event.target.dataset.percent);
        const quantityInput = document.getElementById('quantityInput');

        if (percent === 1)
            quantityInput.value = currentMaxQuantityString;
        else if (currentMaxQuantity > 0)
            quantityInput.value = (currentMaxQuantity * percent).toFixed(8);
    }
});

document.addEventListener('click', (event) => {
    const fromDropdown = document.querySelector('.swap__from .dropdown-list');
    const toDropdown = document.querySelector('.swap__to .dropdown-list');

    if (!event.target.closest('.swap__from') && fromDropdown) {
        fromDropdown.style.display = 'none';
    }
    if (!event.target.closest('.swap__to') && toDropdown) {
        toDropdown.style.display = 'none';
    }
});