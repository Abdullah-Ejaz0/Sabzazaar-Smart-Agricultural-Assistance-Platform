import json
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
from api.supabase_client import supabase
from api.middleware.auth import require_auth, require_expert


@api_view(["GET"])
@require_auth
@require_expert
@require_http_methods(["GET"])
def dashboard_stats(request):
    """GET /api/expert/dashboard/ — returns pending/answered/today/urgent counts."""
    result = supabase.rpc('get_expert_dashboard_stats', {
        'p_expert_id': request.user_id
    }).execute()
    return JsonResponse(result.data[0] if result.data else {})


@api_view(["GET"])
@require_auth
@require_expert
@require_http_methods(["GET"])
def pending_questions(request):
    """GET /api/expert/pending/ — paginated queue of unanswered farmer questions."""
    result = supabase.rpc('get_pending_questions', {
        'p_limit':  int(request.GET.get('limit', 20)),
        'p_offset': int(request.GET.get('offset', 0))
    }).execute()
    return JsonResponse(result.data, safe=False)


@csrf_exempt
@api_view(["POST"])
@require_auth
@require_expert
@require_http_methods(["POST"])
def send_broadcast(request):
    """POST /api/expert/broadcast/ — expert sends an alert broadcast, optionally targeted to a region."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('send_broadcast', {
        'p_expert_id': request.user_id,
        'p_title':     body['title'],
        'p_message':   body['message'],
        'p_region':    body.get('target_region')
    }).execute()
    return JsonResponse({'broadcast_id': result.data}, status=201)
