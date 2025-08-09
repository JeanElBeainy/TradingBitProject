// Collapsible Header Script
const collapsibles = document.querySelectorAll(".collapsible");
collapsibles.forEach((item) => {
    const toggler = item.querySelector('.nav__toggler');
    if(toggler) {
        toggler.addEventListener("click", function () {
            this.parentElement.classList.toggle("collapsible--expanded");
        });
    }
});

// Animation on Scroll Script
function animateNumber(element, target, duration = 2000) {
    const isDecimal = element.dataset.decimal === 'true';
    const suffix = element.dataset.suffix || '';
    const prefix = element.dataset.prefix || '';
    const startTime = performance.now();

    function updateNumber(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeOut = 1 - Math.pow(1 - progress, 3);
        let currentValue = Math.floor((target * easeOut));

        if (isDecimal)
            currentValue = (target * easeOut).toFixed(2);

        element.textContent = prefix + currentValue + suffix;

        if (progress < 1)
            requestAnimationFrame(updateNumber);
    }
    requestAnimationFrame(updateNumber);
}

const universalObserver = new IntersectionObserver((entries) => {
    let delay = 0;
    entries.forEach(entry => {
        if (entry.isIntersecting && !entry.target.classList.contains('animate')) {
            setTimeout(() => {
                entry.target.classList.add('animate');

                const statNumber = entry.target.querySelector('.stats__number');
                if (statNumber) {
                    const target = parseFloat(statNumber.dataset.target);
                    animateNumber(statNumber, target, 2500);
                }
            }, delay);
            delay += 170;
        }
    });
}, { threshold: 0.3 });

document.addEventListener('DOMContentLoaded', () => {
    const footerItems = document.querySelectorAll('.footer__item');
    const statsItems = document.querySelectorAll('.stats__item');
    const infoItems = document.querySelectorAll('.info__item');
    const animateItems = document.querySelectorAll('.animate-item');

    [...footerItems, ...statsItems, ...infoItems, ...animateItems].forEach(item => {
        universalObserver.observe(item);
    });
});