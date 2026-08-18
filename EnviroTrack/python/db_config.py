"""
Shared database configuration for the EnviroTrack Python side.

Credentials are never stored here. They are read, in order of priority:
  1. Environment variables  ENVIROTRACK_DB_HOST / _USER / _PASSWORD / _NAME
  2. A local .env file (git-ignored), if python-dotenv is installed

Both simulator.py and analyze.py import get_connection() from this module,
so the connection details live in exactly one place.

See .env.example for the file format, and SECRETS-SETUP.md for setup.
"""

import os
import sys

import pymysql

# Load .env if python-dotenv is available. It is optional: if you prefer to set
# real environment variables, the import failing changes nothing.
try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass


def _required(key: str) -> str:
    """Fetch a required setting, or exit with an instruction instead of a stack trace."""
    value = os.environ.get(key)
    if not value:
        sys.exit(
            f"\nMissing database setting: {key}\n"
            f"Set it as an environment variable, or copy .env.example to .env and fill it in.\n"
            f"See SECRETS-SETUP.md for step-by-step instructions.\n"
        )
    return value.strip()


def get_connection():
    """Open a connection to the EnviroTrack database."""
    return pymysql.connect(
        host=os.environ.get("ENVIROTRACK_DB_HOST", "localhost").strip(),
        user=_required("ENVIROTRACK_DB_USER"),
        password=_required("ENVIROTRACK_DB_PASSWORD"),
        database=os.environ.get("ENVIROTRACK_DB_NAME", "envirotrack_db").strip(),
    )


def describe_target() -> str:
    """Human-readable connection target. Never includes the password."""
    host = os.environ.get("ENVIROTRACK_DB_HOST", "localhost")
    name = os.environ.get("ENVIROTRACK_DB_NAME", "envirotrack_db")
    user = os.environ.get("ENVIROTRACK_DB_USER", "<unset>")
    return f"{name} on {host} as user {user}"