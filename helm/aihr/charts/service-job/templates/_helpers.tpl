{{- define "service-job.fullname" -}}
{{- printf "%s-job" .Release.Name -}}
{{- end -}}
