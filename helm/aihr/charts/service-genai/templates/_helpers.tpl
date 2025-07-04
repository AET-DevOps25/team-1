{{- define "service-genai.fullname" -}}
{{- printf "%s-genai" .Release.Name -}}
{{- end -}}

{{- define "service-genai.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-genai.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}