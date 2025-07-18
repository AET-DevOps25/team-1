import datetime
import threading

from pythonjsonlogger.json import JsonFormatter


class SpringBootStyleJsonFormatter(JsonFormatter):
    def process_log_record(self, log_record):
        # @timestamp
        log_record['@timestamp'] = datetime.datetime.utcnow().isoformat() + 'Z'
        # @version
        log_record['@version'] = '1'
        # logger_name
        log_record['logger_name'] = log_record.get('name', '')
        # thread_name
        log_record['thread_name'] = threading.current_thread().name
        # level (uppercase to match SpringBoot; will lowercase in pipeline)
        log_record['level'] = log_record.get('levelname', '')
        # level_value
        level_map = {
            'DEBUG': 10000, 'INFO': 20000, 'WARNING': 30000, 'ERROR': 40000, 'CRITICAL': 50000
        }
        log_record['level_value'] = level_map.get(log_record.get('levelname', '').upper(), 0)
        # 保留原 message 字段
        return log_record
