// /home 화면 보조 스크립트: 버튼 이동(data-href), 준비 중 안내(data-soon), 남은 시간, 모바일 메뉴
(function () {
    var toast = document.querySelector('.oa-soon-toast'), timer;

    document.addEventListener('click', function (e) {
        var go = e.target.closest('[data-href]');
        if (go) { location.href = go.getAttribute('data-href'); return; }
        var soon = e.target.closest('[data-soon]');
        if (soon && toast) {
            e.preventDefault();
            toast.textContent = soon.getAttribute('data-soon');
            toast.hidden = false;
            clearTimeout(timer);
            timer = setTimeout(function () { toast.hidden = true; }, 2200);
        }
        var menu = e.target.closest('[data-menu-toggle]');
        if (menu) {
            var nav = document.querySelector('.oa-main-tabs');
            if (nav) nav.classList.toggle('open');
        }
    });

    // 원래 화면과 같은 표시: 1일 이상 "n일 남음", 1일 미만 HH:MM:SS, 시작 전/종료
    function label(starts, ends) {
        var now = Date.now();
        if (now < starts) return '시작 전';
        var left = Math.floor((ends - now) / 1000);
        if (left <= 0) return '종료';
        var d = Math.floor(left / 86400);
        if (d > 0) return d + '일 남음';
        function p(n) { return (n < 10 ? '0' : '') + n; }
        return p(Math.floor(left / 3600)) + ':' + p(Math.floor(left % 3600 / 60)) + ':' + p(left % 60);
    }

    function tick() {
        document.querySelectorAll('.ta-clock[data-ends]').forEach(function (el) {
            var span = el.querySelector('span');
            if (span) span.textContent = label(Number(el.getAttribute('data-starts')), Number(el.getAttribute('data-ends')));
        });
    }

    tick();
    setInterval(tick, 1000);
})();
