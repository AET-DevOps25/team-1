{{- define "service-auth.fullname" -}}
{{- printf "%s-auth" .Release.Name -}}
{{- end -}}

{{- define "service-auth.selectorLabels" -}}
app.kubernetes.io/name: {{ include "service-auth.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}