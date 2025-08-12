function filterTable() {
    let input = document.getElementById("cryptoSearch");
    let table = document.querySelector(".table__section table");
    let tr = table.getElementsByTagName("tr");
    let visibleCount = false;

    let existingMessage = document.getElementById("notFoundMessage");
    if (existingMessage) existingMessage.remove();

    table.classList.remove("no-results");

    for (let i = 1; i < tr.length; i++) {
        let tds = tr[i].getElementsByTagName("td");
        let found = false;
        for (let td of tds) {
            if (td.textContent.toLowerCase().includes(input.value.toLowerCase())) {
                found = true;
                break;
            }
        }
        tr[i].style.display = found ? "" : "none";
        if (found) visibleCount = true;
    }

    if (!visibleCount) {
        table.classList.add("no-results");
        let notFoundRow = document.createElement("tr");
        notFoundRow.id = "notFoundMessage";
        notFoundRow.innerHTML = '<td colspan="9" style="text-align: center; padding: 20px; color: #666;">No cryptocurrencies found matching "' + input.value + '"</td>';
        table.querySelector("tbody").appendChild(notFoundRow);
    }
}