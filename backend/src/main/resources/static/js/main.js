/* ================================================================
   BAD HABITS STORE — MAIN JS
   Vanilla ES5/6, no framework
   ================================================================ */
(function () {
    'use strict';

    const $ = (sel, ctx) => (ctx || document).querySelector(sel);
    const $$ = (sel, ctx) => Array.from((ctx || document).querySelectorAll(sel));

    /* ---------- Announcement bar ---------- */
    (function initAnnouncement() {
        const bar = $('[data-announcement]');
        if (!bar) return;
        const closeBtn = $('[data-announcement-close]', bar);
        if (!closeBtn) return;

        if (sessionStorage.getItem('bh_ann_closed') === '1') {
            bar.classList.add('is-hidden');
        }
        closeBtn.addEventListener('click', () => {
            bar.classList.add('is-hidden');
            sessionStorage.setItem('bh_ann_closed', '1');
        });
    })();

    /* ---------- Header scroll shrink ---------- */
    (function initHeaderScroll() {
        const header = $('[data-header]');
        if (!header) return;
        const onScroll = () => {
            header.classList.toggle('is-scrolled', window.scrollY > 8);
        };
        onScroll();
        window.addEventListener('scroll', onScroll, { passive: true });
    })();

    /* ---------- Mobile menu ---------- */
    (function initMobileMenu() {
        const header = $('[data-header]');
        const toggle = $('[data-mobile-menu-toggle]');
        if (!header || !toggle) return;

        toggle.addEventListener('click', () => {
            const open = header.classList.toggle('is-menu-open');
            document.body.classList.toggle('no-scroll', open);
        });

        // close on nav link tap (mobile)
        $$('.site-header__nav a').forEach(a => {
            a.addEventListener('click', () => {
                if (window.innerWidth <= 1024) {
                    header.classList.remove('is-menu-open');
                    document.body.classList.remove('no-scroll');
                }
            });
        });
    })();

    /* ---------- Cart drawer ---------- */
    (function initCartDrawer() {
        const drawer = $('[data-cart-drawer]');
        if (!drawer) return;

        const open = () => {
            drawer.setAttribute('aria-hidden', 'false');
            document.body.classList.add('no-scroll');
        };
        const close = () => {
            drawer.setAttribute('aria-hidden', 'true');
            document.body.classList.remove('no-scroll');
        };

        $$('[data-cart-drawer-open]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                open();
            });
        });
        $$('[data-cart-drawer-close]').forEach(btn => {
            btn.addEventListener('click', close);
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && drawer.getAttribute('aria-hidden') === 'false') {
                close();
            }
        });
    })();

    /* ---------- Product slider ---------- */
    (function initProductSliders() {
        $$('[data-product-slider]').forEach(slider => {
            const track = $('[data-slider-track]', slider);
            const prev = $('[data-slider-prev]', slider);
            const next = $('[data-slider-next]', slider);
            if (!track) return;

            const update = () => {
                const hasOverflow = track.scrollWidth > track.clientWidth + 1;
                slider.classList.toggle('has-overflow', hasOverflow);
                if (prev) prev.disabled = track.scrollLeft <= 0;
                if (next) next.disabled = Math.ceil(track.scrollLeft + track.clientWidth) >= track.scrollWidth - 1;
            };

            const getSlideStep = () => {
                const firstSlide = $('.product-slider__slide', track);
                if (!firstSlide) return track.clientWidth * 0.9;
                const gap = parseFloat(getComputedStyle(track).columnGap || getComputedStyle(track).gap || 10) || 10;
                return firstSlide.getBoundingClientRect().width + gap;
            };

            const scrollByStep = (dir) => {
                track.scrollBy({ left: dir * track.clientWidth * 0.9, behavior: 'smooth' });
            };

            prev && prev.addEventListener('click', () => scrollByStep(-1));
            next && next.addEventListener('click', () => scrollByStep(1));
            track.addEventListener('scroll', update, { passive: true });
            window.addEventListener('resize', update);

            update();
            requestAnimationFrame(update);
            $$('img', track).forEach(img => {
                if (!img.complete) img.addEventListener('load', update, { once: true });
            });

            // ----- Autoplay: advance 1 slide per tick, loop back to start when done -----
            const autoplayMs = parseInt(slider.getAttribute('data-slider-autoplay') || '0', 10);
            if (autoplayMs > 0) {
                let timer = null;

                const tick = () => {
                    const step = getSlideStep();
                    // at end? (within 2px tolerance)
                    if (track.scrollLeft + track.clientWidth >= track.scrollWidth - 2) {
                        track.scrollTo({ left: 0, behavior: 'smooth' });
                    } else {
                        track.scrollBy({ left: step, behavior: 'smooth' });
                    }
                };

                const start = () => {
                    if (timer) return;
                    timer = setInterval(tick, autoplayMs);
                };
                const stop = () => {
                    if (timer) { clearInterval(timer); timer = null; }
                };

                slider.addEventListener('mouseenter', stop);
                slider.addEventListener('mouseleave', start);
                // pause while user is manually scrolling / touching
                track.addEventListener('touchstart', stop, { passive: true });
                track.addEventListener('touchend', start, { passive: true });

                start();
            }
        });
    })();

    /* ---------- Hero carousel ---------- */
    (function initHero() {
        const hero = $('.hero');
        if (!hero) return;
        const slides = $$('.hero__slide', hero);
        if (slides.length <= 1) return;

        // reset: show first only
        slides.forEach((s, i) => s.classList.toggle('is-active', i === 0));

        // dots
        const dotsWrap = document.createElement('div');
        dotsWrap.className = 'hero__dots';
        const dots = slides.map((_, i) => {
            const d = document.createElement('button');
            d.type = 'button';
            d.className = 'hero__dot' + (i === 0 ? ' is-active' : '');
            d.setAttribute('aria-label', 'Slide ' + (i + 1));
            d.addEventListener('click', () => go(i, true));
            dotsWrap.appendChild(d);
            return d;
        });
        hero.appendChild(dotsWrap);

        let current = 0;
        let timer = null;

        const go = (idx, manual) => {
            slides[current].classList.remove('is-active');
            dots[current].classList.remove('is-active');
            current = (idx + slides.length) % slides.length;
            slides[current].classList.add('is-active');
            dots[current].classList.add('is-active');
            if (manual) restart();
        };

        const next = () => go(current + 1);

        const start = () => { timer = setInterval(next, 5000); };
        const stop  = () => { if (timer) clearInterval(timer); timer = null; };
        const restart = () => { stop(); start(); };

        hero.addEventListener('mouseenter', stop);
        hero.addEventListener('mouseleave', start);

        start();
    })();

    /* ---------- Product detail gallery ---------- */
    (function initGallery() {
        const main = $('[data-main-image]');
        const thumbs = $$('.product-detail__thumb');
        if (!main || thumbs.length === 0) return;

        thumbs.forEach(t => {
            t.addEventListener('click', () => {
                const src = t.getAttribute('data-src');
                if (!src) return;
                main.style.opacity = '0';
                setTimeout(() => {
                    main.src = src;
                    main.style.opacity = '1';
                }, 150);
                thumbs.forEach(x => x.classList.remove('is-active'));
                t.classList.add('is-active');
            });
        });
    })();

    /* ---------- Quantity stepper ---------- */
    (function initQtyStepper() {
        $$('[data-qty-stepper]').forEach(stepper => {
            const input = $('input[type="number"]', stepper);
            const dec = $('[data-qty-dec]', stepper);
            const inc = $('[data-qty-inc]', stepper);
            if (!input) return;

            const min = parseInt(input.min || '1', 10);
            const max = parseInt(input.max || '99', 10);

            dec && dec.addEventListener('click', () => {
                const v = Math.max(min, (parseInt(input.value, 10) || min) - 1);
                input.value = v;
            });
            inc && inc.addEventListener('click', () => {
                const v = Math.min(max, (parseInt(input.value, 10) || min) + 1);
                input.value = v;
            });
        });
    })();

    /* ---------- Product detail: color label binding ---------- */
    (function initColorLabel() {
        const group = $('.product-detail__colors');
        const label = $('[data-selected-color]');
        if (!group || !label) return;

        group.addEventListener('change', (e) => {
            if (e.target && e.target.name === 'color') {
                label.textContent = e.target.value;
            }
        });
    })();

    /* ---------- Checkout: payment panel toggle ---------- */
    (function initPaymentPanel() {
        const group = $('[data-payment-methods]');
        if (!group) return;
        const panels = $$('[data-payment-panel]');

        const update = () => {
            const checked = $('input[name="paymentMethod"]:checked', group);
            const method = checked ? checked.getAttribute('data-method') : null;
            panels.forEach(p => {
                p.hidden = p.getAttribute('data-payment-panel') !== method;
            });
        };
        group.addEventListener('change', update);
        update();
    })();

    /* ---------- Scroll reveal (simple IO) ---------- */
    (function initReveal() {
        const els = $$('[data-reveal]');
        if (!('IntersectionObserver' in window) || els.length === 0) {
            els.forEach(el => el.classList.add('is-visible'));
            return;
        }
        const io = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    io.unobserve(entry.target);
                }
            });
        }, { threshold: 0.12 });
        els.forEach(el => io.observe(el));
    })();

})();
