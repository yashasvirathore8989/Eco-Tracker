// EcoTracker Client JavaScript

document.addEventListener('DOMContentLoaded', function () {
    // 1. Initialize Bootstrap Tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // 2. Pickup request date-time validator & min-date setup
    const preferredAtInput = document.getElementById('preferredAt');
    if (preferredAtInput) {
        const now = new Date();
        now.setHours(now.getHours() + 1);
        const isoString = now.toISOString().slice(0, 16);
        preferredAtInput.min = isoString;

        preferredAtInput.addEventListener('change', function () {
            const selected = new Date(this.value);
            const hour = selected.getHours();
            const feedbackEl = document.getElementById('preferredAtHelp');
            if (hour < 8 || hour >= 19) {
                this.classList.add('is-invalid');
                if (feedbackEl) {
                    feedbackEl.textContent = 'Please choose a time within service hours (08:00 AM - 07:00 PM).';
                    feedbackEl.className = 'text-danger small mt-1';
                }
            } else {
                this.classList.remove('is-invalid');
                if (feedbackEl) {
                    feedbackEl.textContent = 'Service hours: 08:00 AM to 07:00 PM daily.';
                    feedbackEl.className = 'form-text text-muted';
                }
            }
        });
    }

    // 3. Demo Login Credentials Autofill Helper
    window.fillCredentials = function (email, password) {
        const emailInput = document.getElementById('username') || document.getElementById('email');
        const passInput = document.getElementById('password');
        if (emailInput && passInput) {
            emailInput.value = email;
            passInput.value = password;
        }
    };

    // 4. Admin Assign Modal dynamic request ID setup
    const assignModal = document.getElementById('assignModal');
    if (assignModal) {
        assignModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const requestId = button.getAttribute('data-request-id');
            const refNumber = button.getAttribute('data-request-ref');
            const form = document.getElementById('assignForm');
            if (form) {
                form.action = '/admin/requests/' + requestId + '/assign';
            }
            const modalRef = document.getElementById('assignModalRef');
            if (modalRef) {
                modalRef.textContent = refNumber;
            }
        });
    }

    // 5. Admin Reject Modal dynamic request ID setup
    const rejectModal = document.getElementById('rejectModal');
    if (rejectModal) {
        rejectModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const requestId = button.getAttribute('data-request-id');
            const refNumber = button.getAttribute('data-request-ref');
            const form = document.getElementById('rejectForm');
            if (form) {
                form.action = '/admin/requests/' + requestId + '/reject';
            }
            const modalRef = document.getElementById('rejectModalRef');
            if (modalRef) {
                modalRef.textContent = refNumber;
            }
        });
    }

    // 6. Admin Complete Modal dynamic request ID setup
    const completeModal = document.getElementById('completeModal');
    if (completeModal) {
        completeModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const requestId = button.getAttribute('data-request-id');
            const refNumber = button.getAttribute('data-request-ref');
            const initialQty = button.getAttribute('data-request-qty');
            const form = document.getElementById('completeForm');
            if (form) {
                form.action = '/admin/requests/' + requestId + '/complete';
            }
            const modalRef = document.getElementById('completeModalRef');
            if (modalRef) {
                modalRef.textContent = refNumber;
            }
            const weightInput = document.getElementById('materialWeight');
            if (weightInput && initialQty) {
                weightInput.value = initialQty;
            }
        });
    }

    // 7. Collector Record Pickup Modal
    const pickupModal = document.getElementById('pickupModal');
    if (pickupModal) {
        pickupModal.addEventListener('show.bs.modal', function (event) {
            const button = event.relatedTarget;
            const requestId = button.getAttribute('data-request-id');
            const refNumber = button.getAttribute('data-request-ref');
            const qty = button.getAttribute('data-request-qty');
            const form = document.getElementById('pickupForm');
            if (form) {
                form.action = '/collector/requests/' + requestId + '/pickup';
            }
            const modalRef = document.getElementById('pickupModalRef');
            if (modalRef) {
                modalRef.textContent = refNumber;
            }
            const qtyInput = document.getElementById('verifiedQuantity');
            if (qtyInput && qty) {
                qtyInput.value = qty;
            }
        });
    }

    // 8. Auto-dismiss alerts after 5 seconds
    setTimeout(function () {
        const alerts = document.querySelectorAll('.alert-dismissible');
        alerts.forEach(function (alert) {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        });
    }, 6000);
});
