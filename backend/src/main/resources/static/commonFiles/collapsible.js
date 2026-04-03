const collapsibles = document.querySelectorAll(".collapsible");
collapsibles.forEach((item) => {
    const toggler = item.querySelector('.nav__toggler');
    if(toggler) {
        toggler.addEventListener("click", function () {
            this.parentElement.classList.toggle("collapsible--expanded");
        });
    }
});