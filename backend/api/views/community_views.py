import json
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt
from api.supabase_client import supabase
from api.middleware.auth import require_auth


@require_auth
@require_http_methods(["GET"])
def community_feed(request):
    """GET /api/community/ — paginated forum feed with optional category/search filters."""
    result = supabase.rpc('get_community_posts', {
        'p_category': request.GET.get('category'),
        'p_search':   request.GET.get('search'),
        'p_limit':    int(request.GET.get('limit', 20)),
        'p_offset':   int(request.GET.get('offset', 0))
    }).execute()
    return JsonResponse(result.data, safe=False)

@csrf_exempt
@require_auth
@require_http_methods(["POST"])
def submit_post(request):
    """POST /api/community/post/ — farmer submits a new question to the forum."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('submit_community_post', {
        'p_author_id': request.user_id,
        'p_body':      body['body'],
        'p_category':  body.get('category', 'general'),
        'p_photo_url': body.get('photo_url'),
        'p_crop_id':   body.get('crop_id')
    }).execute()
    return JsonResponse({'post_id': result.data}, status=201)


@require_auth
@require_http_methods(["GET"])
def post_detail(request, post_id):
    """GET /api/community/<post_id>/ — single post with all replies, verified answers first."""
    result = supabase.rpc('get_post_detail', {
        'p_post_id': post_id
    }).execute()
    return JsonResponse(result.data, safe=False)


@csrf_exempt
@require_auth
@require_http_methods(["POST"])
def submit_reply(request, post_id):
    """POST /api/community/<post_id>/replies/ — reply to a community post."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('submit_community_reply', {
        'p_post_id':     post_id,
        'p_author_id':   request.user_id,
        'p_body':        body['body'],
        'p_is_verified': body.get('is_verified', False)
    }).execute()
    return JsonResponse({'reply_id': result.data}, status=201)
