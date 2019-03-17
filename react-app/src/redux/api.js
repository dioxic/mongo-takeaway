
export function getOrder(id) {
	return fetch(`/api/order/${id}`);
}

// export function getPosts(userId) {
// 	return fetch(`/api/posts?owner=${userId}`);
// }

export function postOrder(order) {
	return fetch(`/api/order`, {
		method: 'POST',
		headers: {
			'Accept': 'application/json',
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(order)
	});
}