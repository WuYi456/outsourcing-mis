// 全局配置
const API_BASE = '/api';
const TOKEN_KEY = 'token';
const USER_KEY = 'username';
const ROLE_KEY = 'role';

// ========== 工具函数 ==========

// 获取Token
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

// 检查登录状态
function checkAuth() {
    const token = getToken();
    if (!token) {
        window.location.href = '/login';
        return false;
    }
    return true;
}

// 退出登录
function logout() {
    localStorage.clear();
    window.location.href = '/login';
}

// 格式化日期
function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr.replace('T', ' ');
}

// ========== 请求封装 ==========

async function apiRequest(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    const response = await fetch(API_BASE + url, {
        ...options,
        headers
    });

    const result = await response.json();
    if (result.code === 401) {
        localStorage.clear();
        window.location.href = '/login';
        throw new Error('登录已过期');
    }
    return result;
}

// ========== 页面初始化 ==========

document.addEventListener('DOMContentLoaded', function() {
    // 显示用户名
    const username = localStorage.getItem(USER_KEY) || '用户';
    document.querySelectorAll('#usernameDisplay').forEach(el => {
        el.textContent = username;
    });
});