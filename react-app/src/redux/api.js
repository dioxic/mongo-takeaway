
export function getOrder(id) {
	return fetch(`/api/order/${id}`);
}

export function getPosts(userId) {
	return fetch(`/api/posts?owner=${userId}`);
}