document.querySelectorAll("input[type='date']").forEach((el) => {
    if (!el.value && el.name === "arrival") {
        const t = new Date();
        el.valueAsDate = t;
    }
});
