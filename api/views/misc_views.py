from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from api.supabase_client import supabase
from api.middleware.auth import require_auth


@require_auth
@require_http_methods(["GET"])
def chatbot_faqs(request):
    """GET /api/chatbot/faqs/ — returns localised FAQ content for the chatbot."""
    result = supabase.rpc('get_chatbot_faqs', {
        'p_category': request.GET.get('category'),
        'p_language': request.GET.get('language', 'en')
    }).execute()
    return JsonResponse(result.data, safe=False)


@require_auth
@require_http_methods(["GET"])
def broadcasts(request):
    """GET /api/broadcasts/ — returns broadcast alerts for the farmer's region."""
    result = supabase.rpc('get_broadcasts', {
        'p_region': request.GET.get('region'),
        'p_limit':  int(request.GET.get('limit', 10))
    }).execute()
    return JsonResponse(result.data, safe=False)


@require_http_methods(["GET"])
def health_check(request):
    """GET /api/health/ — no auth needed, verifies Django can reach Supabase."""
    try:
        result = supabase.from_('crops').select('name').limit(1).execute()
        return JsonResponse({'status': 'ok', 'db': 'connected'})
    except Exception as e:
        return JsonResponse({'status': 'error', 'detail': str(e)}, status=500)
