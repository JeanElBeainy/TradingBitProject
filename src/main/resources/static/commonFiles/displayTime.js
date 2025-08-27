function updateUserTime() {
    const userTime = new Date().toLocaleString('en-US', {
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
    const lastUpdatedElement = document.getElementById('last-updated');
    if (lastUpdatedElement) {
        lastUpdatedElement.textContent = userTime;
    }
}
updateUserTime();
setInterval(updateUserTime, 60000);