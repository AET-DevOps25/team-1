{{/*
Return chart name
*/}}
{{- define "service-genai.name" -}}
service-genai
{{- end }}

{{/*
Return canonical full name
*/}}
{{- define "service-genai.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "service-genai.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Labels used for selector matchLabels
*/}}
{{- define "service-genai.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-genai.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}