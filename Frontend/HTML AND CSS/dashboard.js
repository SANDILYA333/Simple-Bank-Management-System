// dashboard.js

document.addEventListener("DOMContentLoaded", () => {
    // ------------------------------------------------------
    //  BASIC SESSION CHECK
    // ------------------------------------------------------
    const accountNumber = localStorage.getItem("credoraAccountNumber");
    const email = localStorage.getItem("credoraEmail");

    if (!accountNumber || !email) {
        alert("Please login first to access your dashboard.");
        window.location.href = "login.html";
        return;
    }

    // ------------------------------------------------------
    //  DOM ELEMENTS
    // ------------------------------------------------------
    const customerNameEl         = document.getElementById("customer-name");
    const customerEmailEl        = document.getElementById("customer-email");
    const accountNumberLabelEl   = document.getElementById("account-number-label");
    const lastUpdatedEl          = document.getElementById("last-updated");

    const availableBalanceEl     = document.getElementById("available-balance");
    const accountNumberDisplayEl = document.getElementById("account-number-display");

    const depositForm            = document.getElementById("deposit-form");
    const depositAmountInput     = document.getElementById("deposit-amount");

    const withdrawForm           = document.getElementById("withdraw-form");
    const withdrawAmountInput    = document.getElementById("withdraw-amount");

    const transferForm           = document.getElementById("transfer-form");
    const transferTargetInput    = document.getElementById("transfer-target");
    const transferAmountInput    = document.getElementById("transfer-amount");

    // ------------------------------------------------------
    //  HELPER FUNCTIONS
    // ------------------------------------------------------
    function maskAccountNumber(acc) {
        if (!acc) return "XXXX XXXX XXXX XXXX";
        const s = String(acc);
        if (s.length <= 4) return "XXXX XXXX XXXX " + s;
        return "XXXX XXXX XXXX " + s.slice(-4);
    }

    function formatCurrency(amount) {
        if (amount == null || isNaN(amount)) return "$0.00";
        return "$" + Number(amount).toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function setLastUpdatedNow() {
        lastUpdatedEl.textContent =
            "Last updated: " + new Date().toLocaleTimeString();
    }

    // ------------------------------------------------------
    //  INITIAL STATIC TEXT
    // ------------------------------------------------------
    const nameFromEmail = email.split("@")[0] || "Customer";
    customerNameEl.textContent       = nameFromEmail;
    customerEmailEl.textContent      = email;
    accountNumberLabelEl.textContent = "Account: " + accountNumber;
    accountNumberDisplayEl.textContent = maskAccountNumber(accountNumber);

    // ------------------------------------------------------
    //  BALANCE LOADING
    //  Uses: GET /api/account/balance?accountNumber=CRD00003
    // ------------------------------------------------------
    async function refreshBalance() {
        try {
            const qs = new URLSearchParams({ accountNumber }).toString();
            const data = await callApi("/api/account/balance?" + qs, {
                method: "GET"
            });

            console.log("Balance response:", data);

            const balance = Number(data.balance ?? 0);
            availableBalanceEl.textContent = formatCurrency(balance);
            setLastUpdatedNow();
        } catch (err) {
            console.error("Failed to load balance:", err);
            alert("Could not load account balance. Check if CredoraHttpServer is running.");
        }
    }

    // ------------------------------------------------------
    //  DEPOSIT
    //  POST /api/transaction/deposit
    //  Body: accountNumber, amount (x-www-form-urlencoded)
    // ------------------------------------------------------
    if (depositForm) {
        depositForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const amount = parseFloat(depositAmountInput.value);
            if (isNaN(amount) || amount <= 0) {
                alert("Enter a valid deposit amount.");
                return;
            }

            const body = new URLSearchParams({
                accountNumber: accountNumber,
                amount: amount.toString()
            });

            try {
                await callApi("/api/transaction/deposit", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: body.toString()
                });

                alert("Deposit successful.");
                depositAmountInput.value = "";
                await refreshBalance();
            } catch (err) {
                console.error("Deposit failed:", err);
                // callApi already showed an alert
            }
        });
    }

    // ------------------------------------------------------
    //  WITHDRAW
    //  POST /api/transaction/withdraw
    //  Body: accountNumber, amount (x-www-form-urlencoded)
    // ------------------------------------------------------
    if (withdrawForm) {
        withdrawForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const amount = parseFloat(withdrawAmountInput.value);
            if (isNaN(amount) || amount <= 0) {
                alert("Enter a valid withdrawal amount.");
                return;
            }

            const body = new URLSearchParams({
                accountNumber: accountNumber,
                amount: amount.toString()
            });

            try {
                await callApi("/api/transaction/withdraw", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: body.toString()
                });

                alert("Withdrawal successful.");
                withdrawAmountInput.value = "";
                await refreshBalance();
            } catch (err) {
                console.error("Withdrawal failed:", err);
                // callApi already showed an alert with server message / stack
            }
        });
    }

    // ------------------------------------------------------
    //  TRANSFER
    //  POST /api/transaction/transfer
    //  Body: fromAccountNumber, toAccountNumber, amount (x-www-form-urlencoded)
    // ------------------------------------------------------
    if (transferForm) {
        transferForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const target = transferTargetInput.value.trim();
            const amount = parseFloat(transferAmountInput.value);

            if (!target) {
                alert("Enter a target account number.");
                return;
            }
            if (isNaN(amount) || amount <= 0) {
                alert("Enter a valid transfer amount.");
                return;
            }

            const body = new URLSearchParams({
                fromAccountNumber: accountNumber,
                toAccountNumber: target,
                amount: amount.toString()
            });

            try {
                await callApi("/api/transaction/transfer", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: body.toString()
                });

                alert("Transfer successful.");
                transferTargetInput.value  = "";
                transferAmountInput.value  = "";
                await refreshBalance();
            } catch (err) {
                console.error("Transfer failed:", err);
            }
        });
    }

    // ------------------------------------------------------
    //  INITIAL LOAD
    // ------------------------------------------------------
    refreshBalance();
});
