import json
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt
from api.supabase_client import supabase
from api.middleware.auth import require_auth


@require_auth
@require_http_methods(["GET"])
def latest_soil(request):
    """GET /api/soil/latest/ — returns the most recent soil health reading for the user."""
    result = supabase.rpc('get_latest_soil_health', {
        'p_user_id': request.user_id
    }).execute()
    if not result.data:
        return JsonResponse({}, status=200)
    return JsonResponse(result.data[0])


@csrf_exempt
@require_auth
@require_http_methods(["POST"])
def save_soil(request):
    """POST /api/soil/ — saves a new soil health reading (pH, N, P, K)."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('upsert_soil_health', {
        'p_user_id': request.user_id,
        'p_ph':      body.get('ph'),
        'p_n':       body.get('nitrogen'),
        'p_p':       body.get('phosphorus'),
        'p_k':       body.get('potassium'),
        'p_notes':   body.get('notes')
    }).execute()
    return JsonResponse({'entry_id': result.data}, status=201)
