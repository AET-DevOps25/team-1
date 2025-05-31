{{- define "service-genai.fullname" -}}
{{- printf "%s-genai" .Release.Name -}}
{{- end -}}
