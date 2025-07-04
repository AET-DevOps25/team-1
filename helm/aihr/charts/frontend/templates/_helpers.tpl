{{/*
Return fully-qualified name
*/}}
{{- define "frontend.fullname" -}}
{{ include "aihr.releaseName" . }}-frontend
{{- end }}
