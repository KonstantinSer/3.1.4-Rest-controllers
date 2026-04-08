const API = '/api';
let editModal, deleteModal;

// ===== ИНИЦИАЛИЗАЦИЯ =====
document.addEventListener('DOMContentLoaded', () => {
    editModal = new bootstrap.Modal(document.getElementById('editModal'));
    deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
});

// ===== ПРОВЕРКА АВТОРИЗАЦИИ =====
async function checkAuth() {
    const res = await fetch(`${API}/users/me`, { credentials: 'include' });
    if (!res.ok) {
        window.location.href = '/index.html';
        return false;
    }
    return true;
}

// ===== ЗАГРУЗКА ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ =====
async function loadCurrentUser() {
    const res = await fetch(`${API}/users/me`, { credentials: 'include' });
    return res.ok ? await res.json() : null;
}

// ===== ЗАГРУЗКА ВСЕХ ПОЛЬЗОВАТЕЛЕЙ =====
async function loadUsers() {
    try {
        const res = await fetch(`${API}/users`, { credentials: 'include' });

        if (!res.ok) {
            document.getElementById('usersLoading').innerHTML = `
                <div class="alert alert-danger">
                    Error loading users (${res.status}). Make sure you have ADMIN role.
                </div>
            `;
            return;
        }

        const users = await res.json();

        document.getElementById('usersLoading').style.display = 'none';
        document.getElementById('usersTable').style.display = 'table';

        const tbody = document.getElementById('usersTableBody');
        tbody.innerHTML = users.map(user => `
            <tr>
                <td>${user.id}</td>
                <td>${user.firstName}</td>
                <td>${user.lastName}</td>
                <td>${user.age}</td>
                <td>${user.email}</td>
                <td>${user.roles.map(r => r.name.replace('ROLE_', '')).join(', ')}</td>
                <td>
                    <button class="btn btn-info btn-sm text-white me-2" onclick="openEdit(${user.id})">
                        ✏️ Edit
                    </button>
                    <button class="btn btn-danger btn-sm" onclick="openDelete(${user.id}, '${user.firstName} ${user.lastName}')">
                        🗑️ Delete
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        document.getElementById('usersLoading').innerHTML = `
            <div class="alert alert-danger">Error: ${error.message}</div>
        `;
    }
}

// ===== ЗАГРУЗКА РОЛЕЙ =====
async function loadRoles() {
    const res = await fetch(`${API}/roles`);
    const roles = res.ok ? await res.json() : [];

    ['newRoles', 'editRoles'].forEach(id => {
        const select = document.getElementById(id);
        if (select) {
            select.innerHTML = roles.map(r =>
                `<option value="${r.id}">${r.name.replace('ROLE_', '')}</option>`
            ).join('');
        }
    });

    return roles;
}

// ===== СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ =====
if (document.getElementById('createUserForm')) {
    document.getElementById('createUserForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        const selectedRoles = Array.from(document.getElementById('newRoles').selectedOptions)
            .map(opt => parseInt(opt.value));

        const userData = {
            firstName: document.getElementById('newFirstName').value,
            lastName: document.getElementById('newLastName').value,
            age: parseInt(document.getElementById('newAge').value),
            email: document.getElementById('newEmail').value,
            password: document.getElementById('newPassword').value,
            roleIds: selectedRoles
        };

        try {
            const res = await fetch(`${API}/users`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(userData)
            });

            if (res.ok) {
                alert('✅ User created successfully!');
                document.getElementById('createUserForm').reset();
                switchTab('table');
                loadUsers();
            } else {
                alert('❌ Error creating user');
            }
        } catch (error) {
            alert('❌ Error: ' + error.message);
        }
    });
}

// ===== ОТКРЫТИЕ РЕДАКТИРОВАНИЯ =====
async function openEdit(id) {
    try {
        const res = await fetch(`${API}/users/${id}`, { credentials: 'include' });
        const user = await res.json();

        document.getElementById('editId').value = user.id;
        document.getElementById('editIdDisplay').value = user.id;
        document.getElementById('editFirst').value = user.firstName;
        document.getElementById('editLast').value = user.lastName;
        document.getElementById('editAge').value = user.age;
        document.getElementById('editEmail').value = user.email;
        document.getElementById('editPassword').value = '';

        const select = document.getElementById('editRoles');
        Array.from(select.options).forEach(opt => {
            opt.selected = user.roles.some(r => r.id === parseInt(opt.value));
        });

        editModal.show();
    } catch (error) {
        alert('❌ Error loading user: ' + error.message);
    }
}

// ===== СОХРАНЕНИЕ РЕДАКТИРОВАНИЯ =====
async function saveEdit() {
    const id = document.getElementById('editId').value;
    const password = document.getElementById('editPassword').value;

    const userData = {
        firstName: document.getElementById('editFirst').value,
        lastName: document.getElementById('editLast').value,
        age: parseInt(document.getElementById('editAge').value),
        email: document.getElementById('editEmail').value,
        roleIds: Array.from(document.getElementById('editRoles').selectedOptions)
            .map(opt => parseInt(opt.value))
    };

    if (password && password.trim() !== '') {
        userData.password = password;
    }

    try {
        const res = await fetch(`${API}/users/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(userData)
        });

        if (res.ok) {
            alert('✅ User updated successfully!');
            editModal.hide();
            loadUsers();
        } else {
            alert('❌ Error updating user');
        }
    } catch (error) {
        alert('❌ Error: ' + error.message);
    }
}

// ===== ОТКРЫТИЕ УДАЛЕНИЯ =====
function openDelete(id, name) {
    document.getElementById('deleteId').value = id;
    document.getElementById('deleteName').textContent = name;
    deleteModal.show();
}

// ===== ПОДТВЕРЖДЕНИЕ УДАЛЕНИЯ =====
async function confirmDelete() {
    const id = document.getElementById('deleteId').value;

    try {
        const res = await fetch(`${API}/users/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        });

        if (res.ok) {
            alert('✅ User deleted successfully!');
            deleteModal.hide();
            loadUsers();
        } else {
            alert('❌ Error deleting user');
        }
    } catch (error) {
        alert('❌ Error: ' + error.message);
    }
}

// ===== ПЕРЕКЛЮЧЕНИЕ ВКЛАДОК =====
function switchTab(tab) {
    document.getElementById('tableTab').classList.toggle('d-none', tab !== 'table');
    document.getElementById('formTab').classList.toggle('d-none', tab !== 'form');

    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
    });
    event.target.classList.add('active');
}

// ===== ЛОГАУТ =====
async function logout() {
    await fetch('/logout', { method: 'POST', credentials: 'include' });
    window.location.href = '/index.html?logout';
}

// ===== ЗАГРУЗКА ПРИ СТАРТЕ (для admin.html) =====
if (document.getElementById('usersTable')) {
    checkAuth().then(async (auth) => {
        if (auth) {
            const [currentUser, roles] = await Promise.all([
                loadCurrentUser(),
                loadRoles()
            ]);

            if (currentUser) {
                document.getElementById('currentUserEmail').textContent = currentUser.email;
                document.getElementById('currentUserRoles').textContent =
                    currentUser.roles.map(r => r.name.replace('ROLE_', '')).join(' ');
            }

            loadUsers();
        }
    });
}