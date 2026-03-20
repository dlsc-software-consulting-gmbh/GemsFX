/* ============================================================
   GemsFX Website — Main JavaScript
   ============================================================ */

(function () {
  'use strict';

  // ── Tab switcher (Maven / Gradle) ──────────────────────────
  document.querySelectorAll('.tabs').forEach(function (tabs) {
    tabs.querySelectorAll('.tab-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        var target = btn.dataset.tab;
        tabs.querySelectorAll('.tab-btn').forEach(function (b) { b.classList.remove('active'); });
        tabs.querySelectorAll('.tab-panel').forEach(function (p) { p.classList.remove('active'); });
        btn.classList.add('active');
        tabs.querySelector('.tab-panel[data-tab="' + target + '"]').classList.add('active');
      });
    });
  });

  // ── Copy-to-clipboard ─────────────────────────────────────
  document.querySelectorAll('.copy-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var panel = btn.closest('.tab-panel');
      var text = panel ? panel.querySelector('pre').innerText : '';
      navigator.clipboard.writeText(text).then(function () {
        var orig = btn.textContent;
        btn.textContent = '✓ Copied';
        btn.classList.add('copied');
        setTimeout(function () {
          btn.textContent = orig;
          btn.classList.remove('copied');
        }, 1800);
      }).catch(function () {
        /* fallback for non-https */
        var ta = document.createElement('textarea');
        ta.value = text;
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        btn.textContent = '✓ Copied';
        btn.classList.add('copied');
        setTimeout(function () { btn.textContent = 'Copy'; btn.classList.remove('copied'); }, 1800);
      });
    });
  });

  // ── Gallery: filter + search ───────────────────────────────
  var cards        = Array.from(document.querySelectorAll('.control-card'));
  var pills        = Array.from(document.querySelectorAll('.pill[data-filter]'));
  var searchInput  = document.getElementById('search-input');
  var noResults    = document.querySelector('.no-results');
  var activeFilter = 'all';

  function applyFilters() {
    var query = searchInput ? searchInput.value.trim().toLowerCase() : '';
    var visible = 0;

    cards.forEach(function (card) {
      var cat    = card.dataset.category || '';
      var name   = (card.dataset.name   || '').toLowerCase();
      var desc   = (card.dataset.desc   || '').toLowerCase();
      var tags   = (card.dataset.tags   || '').toLowerCase();

      var catMatch    = activeFilter === 'all' || cat === activeFilter;
      var searchMatch = !query || name.indexOf(query) !== -1 || desc.indexOf(query) !== -1 || tags.indexOf(query) !== -1;

      if (catMatch && searchMatch) {
        card.setAttribute('data-visible', '1');
        visible++;
      } else {
        card.removeAttribute('data-visible');
      }
    });

    if (noResults) {
      if (visible === 0) {
        noResults.classList.add('visible');
      } else {
        noResults.classList.remove('visible');
      }
    }
  }

  pills.forEach(function (pill) {
    pill.addEventListener('click', function () {
      activeFilter = pill.dataset.filter;
      pills.forEach(function (p) { p.classList.remove('active'); });
      pill.classList.add('active');
      applyFilters();
    });
  });

  if (searchInput) {
    searchInput.addEventListener('input', applyFilters);
  }

  // initialise — show all cards
  applyFilters();

  // ── Smooth scroll for anchor nav ──────────────────────────
  document.querySelectorAll('a[href^="#"]').forEach(function (link) {
    link.addEventListener('click', function (e) {
      var target = document.querySelector(link.getAttribute('href'));
      if (target) {
        e.preventDefault();
        target.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });
  });

})();
