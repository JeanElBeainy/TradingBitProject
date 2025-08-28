function setSwapDate() {
    const dateInput = document.querySelector('input[name="date"]');
    if (dateInput) {
        dateInput.value = new Date().toLocaleString('en-US', {
            month: 'short',
            day: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    }
}
document.addEventListener('submit', setSwapDate);