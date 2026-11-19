(function () {
  const messageModalElement = document.getElementById("bitfixMessageModal");
  const confirmModalElement = document.getElementById("bitfixConfirmModal");
  let pendingSubmitter = null;

  function showMessage(message, options) {
    if (!messageModalElement || !window.bootstrap) {
      console.warn(message);
      return;
    }

    const settings = options || {};
    const title = document.getElementById("bitfixMessageTitle");
    const text = document.getElementById("bitfixMessageText");
    const icon = document.getElementById("bitfixMessageIcon");
    const type = settings.type || "info";
    const iconClasses = {
      info: ["bi-info-circle-fill", ""],
      warning: ["bi-exclamation-triangle-fill", "bitfix-message-icon-warning"],
      error: ["bi-x-circle-fill", "bitfix-message-icon-error"],
      success: ["bi-check-circle-fill", "bitfix-message-icon-success"]
    };
    const selectedIcon = iconClasses[type] || iconClasses.info;

    title.textContent = settings.title || (type === "error" ? "No se pudo completar" : "Aviso");
    text.textContent = message;
    icon.className = "bitfix-message-icon " + selectedIcon[1];
    icon.innerHTML = "<i class=\"bi " + selectedIcon[0] + "\"></i>";
    window.bootstrap.Modal.getOrCreateInstance(messageModalElement).show();
  }

  function askForConfirmation(message, submitter) {
    if (!confirmModalElement || !window.bootstrap) {
      return;
    }

    pendingSubmitter = submitter;
    document.getElementById("bitfixConfirmText").textContent = message;
    window.bootstrap.Modal.getOrCreateInstance(confirmModalElement).show();
  }

  window.BitFixMessages = {
    show: showMessage
  };

  document.addEventListener("click", function (event) {
    const submitter = event.target.closest("[data-bitfix-confirm]");
    if (!submitter) {
      return;
    }

    event.preventDefault();
    askForConfirmation(submitter.getAttribute("data-bitfix-confirm"), submitter);
  });

  document.addEventListener("DOMContentLoaded", function () {
    const acceptButton = document.getElementById("bitfixConfirmAccept");
    if (!acceptButton) {
      return;
    }

    acceptButton.addEventListener("click", function () {
      const submitter = pendingSubmitter;
      pendingSubmitter = null;
      window.bootstrap.Modal.getOrCreateInstance(confirmModalElement).hide();

      if (submitter && submitter.form) {
        submitter.form.requestSubmit(submitter);
      }
    });

    confirmModalElement.addEventListener("hidden.bs.modal", function () {
      pendingSubmitter = null;
    });
  });
})();
