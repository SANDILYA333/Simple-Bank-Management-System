// Script.js
// Disable old prompt-based logic on open-account.html and login.html
const currentPage = window.location.pathname;

const DISABLE_PROMPTS =
    currentPage.includes("open-account.html") ||
    currentPage.includes("login.html");

document.addEventListener("DOMContentLoaded", () => {
    /*Smooth scroll for nav links*/
    const navLinks = document.querySelectorAll('.nav-links a[href^="#"]');

    navLinks.forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            const targetId = link.getAttribute("href").slice(1);
            const target = document.getElementById(targetId);
            if (!target) return;

            const navHeight = document.querySelector("nav").offsetHeight;
            const top = target.getBoundingClientRect().top + window.pageYOffset - navHeight - 20;

            window.scrollTo({
                top,
                behavior: "smooth"
            });
        });
    });

    /* Shrink / elevate navbar on scroll*/
    const nav = document.querySelector("nav");
    let lastScrollY = window.scrollY;

    const handleNavOnScroll = () => {
        const currentY = window.scrollY;

        // Add subtle shadow + tighter padding when scrolled
        if (currentY > 20) {
            nav.style.boxShadow = "0 10px 30px rgba(0,0,0,0.6)";
            nav.style.padding = "14px 60px";
        } else {
            nav.style.boxShadow = "none";
            nav.style.padding = "20px 60px";
        }

        lastScrollY = currentY;
    };

    window.addEventListener("scroll", handleNavOnScroll);

    /* Mobile menu toggle (no CSS edits)*/
    const mobileMenuIcon = document.querySelector(".nav-mobile .ri-menu-3-line");
    const navLinksContainer = document.querySelector(".nav-links");

    if (mobileMenuIcon && navLinksContainer) {
        mobileMenuIcon.addEventListener("click", () => {
            const currentDisplay = window.getComputedStyle(navLinksContainer).display;
            if (currentDisplay === "none") {
                navLinksContainer.style.display = "flex";
                navLinksContainer.style.flexDirection = "column";
                navLinksContainer.style.position = "absolute";
                navLinksContainer.style.top = nav.offsetHeight + "px";
                navLinksContainer.style.right = "20px";
                navLinksContainer.style.background = "rgba(10,14,20,0.98)";
                navLinksContainer.style.padding = "16px 24px";
                navLinksContainer.style.borderRadius = "12px";
                navLinksContainer.style.border = "1px solid var(--card-border)";
                navLinksContainer.style.gap = "16px";
                navLinksContainer.style.zIndex = "999";
            } else {
                navLinksContainer.style.display = "none";
            }
        });

        // Auto-hide menu when link clicked on mobile
        navLinks.forEach(link => {
            link.addEventListener("click", () => {
                if (window.innerWidth <= 768) {
                    navLinksContainer.style.display = "none";
                }
            });
        });
    }

    /* TypeWriter effect on hero title */
    const heroTitle = document.querySelector(".hero-left h1");
    if (heroTitle) {
        const fullText = heroTitle.textContent.trim();
        heroTitle.textContent = "";
        let index = 0;

        const typeInterval = setInterval(() => {
            heroTitle.textContent = fullText.slice(0, index);
            index++;
            if (index > fullText.length) clearInterval(typeInterval);
        }, 60); // speed
    }

    /*5. Button micro-interactions */
    const buttons = document.querySelectorAll(".btn");
    buttons.forEach(btn => {
        btn.style.transition = (btn.style.transition || "") + ", transform 0.1s ease";

        btn.addEventListener("mousedown", () => {
            btn.style.transform = "scale(0.97)";
        });

        ["mouseup", "mouseleave"].forEach(evt => {
            btn.addEventListener(evt, () => {
                btn.style.transform = "scale(1)";
            });
        });
    });

    /* 6. Counters for dashboard numbers*/
    const statValues = document.querySelectorAll(".stat-item .stat-text p, .account-preview .amount");

    const animateValue = (el, start, end, duration) => {
        const startTime = performance.now();

        const step = (now) => {
            const progress = Math.min((now - startTime) / duration, 1);
            const value = Math.floor(start + (end - start) * progress);

            // If it originally had $ or % keep them
            const text = el.dataset.originalText || el.textContent.trim();
            const hasDollar = text.includes("$");
            const hasPercent = text.includes("%");

            let formatted = value.toLocaleString();
            if (hasDollar) formatted = "$" + formatted;
            if (hasPercent) formatted = formatted + "%";

            el.textContent = formatted;

            if (progress < 1) requestAnimationFrame(step);
        };

        requestAnimationFrame(step);
    };

    statValues.forEach(el => {
        const original = el.textContent.trim();
        el.dataset.originalText = original;

        // Extract numeric part
        const match = original.replace(/,/g, "").match(/[\d.]+/);
        if (!match) return; // skip non-numerics

        const target = parseFloat(match[0]);
        el.textContent = original.includes("%") ? "0%" : original.includes("$") ? "$0" : "0";

        // Animate when section scrolls into view
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    animateValue(el, 0, target, 1200);
                    observer.unobserve(el);
                }
            });
        }, { threshold: 0.5 });

        observer.observe(el);
    });

    /* 7. Subtle scroll reveal for Why Us card */
    const whyUsCard = document.querySelector(".why-us-card");
    if (whyUsCard) {
        // start hidden
        whyUsCard.style.opacity = "0";
        whyUsCard.style.transform = "translateY(20px)";
        whyUsCard.style.transition = "opacity 0.6s ease, transform 0.6s ease";

        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    whyUsCard.style.opacity = "1";
                    whyUsCard.style.transform = "translateY(0)";
                    observer.unobserve(whyUsCard);
                }
            });
        }, { threshold: 0.3 });

        observer.observe(whyUsCard);
    }

    /* PARTICLE BACKGROUND */
    const canvas = document.getElementById("particle-canvas");
    const ctx = canvas.getContext("2d");

    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    let particles = [];

    for (let i = 0; i < 80; i++) {
        particles.push({
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            r: Math.random() * 2 + 1,
            dx: (Math.random() - 0.5) * 0.3,
            dy: (Math.random() - 0.5) * 0.3
        });
    }

    function animateParticles() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        particles.forEach(p => {
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
            ctx.fillStyle = "rgba(255,255,255,0.35)";
            ctx.fill();

            p.x += p.dx;
            p.y += p.dy;

            if (p.x < 0 || p.x > canvas.width) p.dx *= -1;
            if (p.y < 0 || p.y > canvas.height) p.dy *= -1;
        });

        requestAnimationFrame(animateParticles);
    }
    animateParticles();


    /* ---------------------------------------------
       ANIMATED COUNTERS
    --------------------------------------------- */
    const counters = document.querySelectorAll(".stat-text p");
    const counterValues = [12450, 8620, 4.5];

    function animateCounters() {
        counters.forEach((counter, i) => {
            let start = 0;
            let end = counterValues[i];
            let duration = 1500;
            let increment = end / (duration / 16);

            function update() {
                start += increment;
                if (start >= end) start = end;
                counter.innerText = i === 2 ? start.toFixed(1) + "%" : "$" + Math.floor(start);
                if (start < end) requestAnimationFrame(update);
            }
            update();
        });
    }

    setTimeout(animateCounters, 1400);


    /* ---------------------------------------------
       NAV SHRINK ON SCROLL (background fade)
    --------------------------------------------- */
    window.addEventListener("scroll", () => {
        const nav2 = document.querySelector("nav");
        if (window.scrollY > 40) {
            nav2.style.padding = "10px 40px";
            nav2.style.background = "rgba(10,14,20,0.85)";
        } else {
            nav2.style.padding = "20px 60px";
        }
    });


    /* SCROLL REVEAL*/
    const revealElements = document.querySelectorAll(".section-wrapper, .info-card, .testimonial-card, .why-us-card");

    function revealOnScroll() {
        revealElements.forEach(el => {
            const rect = el.getBoundingClientRect();
            if (rect.top < window.innerHeight - 50) {
                el.classList.add("visible");
            }
        });
    }

    window.addEventListener("scroll", revealOnScroll);
    revealOnScroll();

    /* =======================================================
       STEP 2: FRONTEND ↔ BACKEND INTEGRATION (CREDORA BANK)
       - Uses your HttpServer at http://localhost:8080
    ========================================================*/

    const API_BASE_URL = "http://localhost:8080";

    // Generic helper to call backend (JSON-based)
    

// Make it global
window.callApi = async function (path, options = {}) {
    try {
        const res = await fetch(API_BASE_URL + path, {
            // default headers
            headers: {
                
                ...(options.headers || {})   // allow override (like x-www-form-urlencoded)
            },
            ...options
        });

        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || `HTTP ${res.status}`);
        }

        const contentType = res.headers.get("content-type") || "";
        if (contentType.includes("application/json")) {
            return await res.json();
        } else {
            return await res.text();
        }
    } catch (err) {
        console.error("API error:", err);
        alert("Something went wrong while talking to Credora server.\n" + err.message);
        throw err;
    }
};


    // Grab existing hero buttons
    const openAccountBtn = document.querySelector(".cta-buttons .btn-primary");
    const loginBtn = document.querySelector(".cta-buttons .btn-secondary");

    /* ---------------------------
       OPEN ACCOUNT → BACKEND
    --------------------------- */
    if (openAccountBtn && !DISABLE_PROMPTS) {
        openAccountBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            window.location.href = "open-account.html";

            const fullName = prompt("Enter your full name:");
            if (!fullName) return;

            const email = prompt("Enter your email:");
            if (!email) return;

            const password = prompt("Set a password:");
            if (!password) return;

            const initialDepositStr = prompt("Initial deposit amount:");
            if (!initialDepositStr) return;
            const initialDeposit = parseFloat(initialDepositStr);
            if (isNaN(initialDeposit) || initialDeposit < 0) {
                alert("Invalid amount.");
                return;
            }

            const payload = {
                fullName,
                email,
                password,
                initialDeposit
            };

            try {
                // You’ll map this to a real endpoint in HttpServer
                const data = await callApi("/api/accounts/open", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });

                console.log("Account created:", data);

                const accountNumber =
                    (data && (data.accountNumber || data.accountNo || data.account_number)) ||
                    "Unknown";

                alert(
                    "Account created successfully!\n" +
                    "Your account number: " + accountNumber
                );

                localStorage.setItem("credoraAccountNumber", accountNumber);
                localStorage.setItem("credoraEmail", email);
            } catch (err) {
                // already handled in callApi
            }
        });
    }

    /* ---------------------------
       LOGIN → BACKEND
    --------------------------- */
    if (loginBtn && !DISABLE_PROMPTS)  {
        loginBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            window.location.href = "login.html";

            const email = prompt("Enter your registered email:");
            if (!email) return;

            const password = prompt("Enter your password:");
            if (!password) return;

            const payload = { email, password };

            try {
                // You’ll map this to a real endpoint in HttpServer
                const data = await callApi("/api/auth/login", {
                    method: "POST",
                    body: JSON.stringify(payload)
                });

                console.log("Login response:", data);

                const token = data.token || data.jwt || null;
                const accountNumber =
                    (data.accountNumber || data.accountNo || data.account_number || null);

                if (token) {
                    localStorage.setItem("credoraToken", token);
                }
                if (accountNumber) {
                    localStorage.setItem("credoraAccountNumber", accountNumber);
                }
                localStorage.setItem("credoraEmail", email);

                alert("Login successful! Your dashboard is now linked to the backend.");
            } catch (err) {
                // already handled in callApi
            }
        });
    }

});
