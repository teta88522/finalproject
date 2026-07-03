(function(window, document) {
    'use strict';

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function fireSweetAlert(options) {
        return window.Swal.fire({
            icon: options.icon || 'warning',
            title: options.title || '확인',
            html: '<p class="pf-swal-main-text">' + escapeHtml(options.message) + '</p>',
            showCancelButton: !!options.showCancelButton,
            confirmButtonText: options.confirmText || '확인',
            cancelButtonText: options.cancelText || '취소',
            reverseButtons: true,
            buttonsStyling: false,
            iconColor: options.iconColor || '#f9735b',
            customClass: {
                popup: 'pf-swal-popup',
                title: 'pf-swal-title',
                htmlContainer: 'pf-swal-text',
                actions: 'pf-swal-actions',
                confirmButton: options.confirmClass || 'pf-swal-confirm',
                cancelButton: 'pf-swal-cancel'
            }
        });
    }

    function confirmDialog(options) {
        var message = options && options.message ? options.message : '처리하시겠습니까?';

        if (!window.Swal) {
            return Promise.resolve(window.confirm(message));
        }

        return fireSweetAlert({
            icon: options.icon || 'warning',
            title: options.title || '삭제 확인',
            message: message,
            confirmText: options.confirmText || '삭제',
            cancelText: options.cancelText || '취소',
            showCancelButton: true,
            confirmClass: options.confirmClass || 'pf-swal-confirm'
        }).then(function(result) {
            return result.isConfirmed;
        });
    }

    function alertDialog(message, options) {
        var config = options || {};

        if (!window.Swal) {
            window.alert(message);
            return Promise.resolve();
        }

        return fireSweetAlert({
            icon: config.icon || 'info',
            title: config.title || '알림',
            message: message,
            confirmText: config.confirmText || '확인',
            showCancelButton: false,
            iconColor: config.iconColor || '#2b7bbb',
            confirmClass: config.confirmClass || 'pf-swal-confirm pf-swal-confirm-primary'
        });
    }

    function readConfirmOptions(form) {
        return {
            title: form.getAttribute('data-confirm-title') || '삭제 확인',
            message: form.getAttribute('data-confirm-message') || '삭제하시겠습니까?',
            confirmText: form.getAttribute('data-confirm-confirm-text') || '삭제',
            cancelText: form.getAttribute('data-confirm-cancel-text') || '취소',
            icon: form.getAttribute('data-confirm-icon') || 'warning'
        };
    }

    function submitAfterConfirm(form) {
        confirmDialog(readConfirmOptions(form)).then(function(confirmed) {
            if (!confirmed) {
                return;
            }

            form.dispatchEvent(new CustomEvent('pf:confirmed-submit', {
                bubbles: true
            }));

            form.dataset.pfConfirmApproved = 'true';
            if (typeof form.requestSubmit === 'function') {
                form.requestSubmit();
                window.setTimeout(function() {
                    delete form.dataset.pfConfirmApproved;
                }, 0);
                return;
            }

            form.submit();
        });
    }

    function bindConfirmForms(root) {
        var scope = root || document;

        scope.querySelectorAll('.js-pf-confirm-submit').forEach(function(form) {
            if (form.dataset.pfConfirmBound === 'true') {
                return;
            }

            form.dataset.pfConfirmBound = 'true';
            form.addEventListener('submit', function(event) {
                if (form.dataset.pfConfirmApproved === 'true') {
                    return;
                }

                event.preventDefault();
                submitAfterConfirm(form);
            });
        });
    }

    window.PFDialog = {
        alert: alertDialog,
        confirm: confirmDialog,
        bindConfirmForms: bindConfirmForms
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            bindConfirmForms(document);
        });
    } else {
        bindConfirmForms(document);
    }
})(window, document);
