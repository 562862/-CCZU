let currentPage = 1;
const pageSize = 10;

document.addEventListener('DOMContentLoaded', () => {
    loadFilters();
    loadList();

    document.getElementById('keyword').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') search();
    });

    document.getElementById('college').addEventListener('change', () => search());
    document.getElementById('category').addEventListener('change', () => search());
    document.getElementById('level').addEventListener('change', () => search());
});

function loadFilters() {
    fetch('/api/colleges')
        .then(res => res.json())
        .then(list => {
            const sel = document.getElementById('college');
            list.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                sel.appendChild(opt);
            });
        })
        .catch(() => {});

    fetch('/api/categories')
        .then(res => res.json())
        .then(list => {
            const sel = document.getElementById('category');
            list.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                sel.appendChild(opt);
            });
        })
        .catch(() => {});

    fetch('/api/levels')
        .then(res => res.json())
        .then(list => {
            const sel = document.getElementById('level');
            list.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c;
                opt.textContent = c;
                sel.appendChild(opt);
            });
        })
        .catch(() => {});
}

function search() {
    currentPage = 1;
    loadList();
}

function loadList() {
    const keyword = document.getElementById('keyword').value.trim();
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const college = document.getElementById('college').value;
    const category = document.getElementById('category').value;
    const level = document.getElementById('level').value;

    const params = new URLSearchParams();
    if (keyword) params.set('keyword', keyword);
    if (startDate) params.set('startDate', startDate);
    if (endDate) params.set('endDate', endDate);
    if (college) params.set('college', college);
    if (category) params.set('category', category);
    if (level) params.set('level', level);
    params.set('page', currentPage);
    params.set('size', pageSize);

    const cardList = document.getElementById('cardList');
    cardList.innerHTML = '<div class="loading"><div class="loader"></div><span>正在获取数据</span></div>';

    fetch('/api/competitions?' + params.toString())
        .then(res => res.json())
        .then(data => {
            renderList(data.list);
            renderPagination(data.page, data.totalPages, data.total);
            const totalEl = document.getElementById('totalCount');
            if (totalEl) totalEl.textContent = data.total || 0;
            const badgeEl = document.getElementById('resultBadge');
            if (badgeEl) badgeEl.textContent = data.total > 0 ? data.total + ' 条结果' : '';
        })
        .catch(() => {
            cardList.innerHTML = '<div class="empty">加载失败，请稍后重试</div>';
        });
}

function renderList(list) {
    const cardList = document.getElementById('cardList');

    if (!list || list.length === 0) {
        cardList.innerHTML = '<div class="empty">暂无竞赛公告</div>';
        return;
    }

    cardList.innerHTML = list.map(item => `
        <a class="card" href="detail.html?id=${item.id}">
            <div class="icon">🏆</div>
            <div class="info">
                <div class="title">${escapeHtml(item.title)}</div>
                <div class="meta">
                    ${item.college ? '<span class="badge-college">' + escapeHtml(item.college) + '</span>' : ''}
                    ${item.category ? '<span class="badge-category">' + escapeHtml(item.category) + '</span>' : ''}
                    ${item.level ? '<span class="badge-level badge-level-' + levelClass(item.level) + '">' + escapeHtml(item.level) + '</span>' : ''}
                    ${item.publishDate ? '<span class="date-icon">📅</span><span>' + item.publishDate + '</span>' : ''}
                    ${item.deadline ? '<span class="deadline-icon">⏰</span><span class="deadline-text">截止 ' + item.deadline + '</span>' : ''}
                </div>
            </div>
            <span class="arrow">›</span>
        </a>
    `).join('');
}

function renderPagination(page, totalPages, total) {
    const el = document.getElementById('pagination');

    if (totalPages <= 1) {
        el.innerHTML = total > 0 ? `<span class="info">共 ${total} 条</span>` : '';
        return;
    }

    el.innerHTML = `
        <button ${page <= 1 ? 'disabled' : ''} onclick="goPage(${page - 1})">上一页</button>
        <span class="info">第 ${page} / ${totalPages} 页，共 ${total} 条</span>
        <button ${page >= totalPages ? 'disabled' : ''} onclick="goPage(${page + 1})">下一页</button>
    `;
}

function goPage(page) {
    currentPage = page;
    loadList();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function levelClass(level) {
    if (level === '国赛') return 'national';
    if (level === '省赛') return 'provincial';
    if (level === '校赛') return 'school';
    return 'other';
}
