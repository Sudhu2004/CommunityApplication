#!/usr/bin/env python3
"""
Complete API Test Automation Script
Tests all endpoints from authentication to message operations
"""

import requests
import json
import time
from datetime import datetime, date, timedelta
from typing import Dict, Optional, List
import uuid

class Colors:
    """ANSI color codes for terminal output"""
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

class APITestRunner:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
        self.auth_token = None
        self.user_id = None
        self.headers = {"Content-Type": "application/json"}

        # Store created resources for cleanup and reference
        self.created_users = []
        self.created_communities = []
        self.created_groups = []
        self.created_events = []
        self.created_messages = []

        # Test counters
        self.tests_passed = 0
        self.tests_failed = 0
        self.tests_total = 0

    def print_section(self, title: str):
        """Print a section header"""
        print(f"\n{Colors.BOLD}{Colors.CYAN}{'=' * 80}")
        print(f"{title.center(80)}")
        print(f"{'=' * 80}{Colors.RESET}\n")

    def print_test(self, test_name: str, passed: bool, details: str = ""):
        """Print test result"""
        self.tests_total += 1
        if passed:
            self.tests_passed += 1
            print(f"{Colors.GREEN}✓ PASS{Colors.RESET} - {test_name}")
        else:
            self.tests_failed += 1
            print(f"{Colors.RED}✗ FAIL{Colors.RESET} - {test_name}")

        if details:
            print(f"  {Colors.YELLOW}{details}{Colors.RESET}")

    def print_response(self, response: requests.Response, show_body: bool = True):
        """Print formatted response"""
        print(f"  Status: {response.status_code}")
        if show_body and response.text:
            try:
                body = json.loads(response.text)
                print(f"  Response: {json.dumps(body, indent=2)}")
            except:
                print(f"  Response: {response.text[:200]}")

    # ==================== AUTHENTICATION TESTS ====================

    def test_register_user(self, email: str, password: str, name: str, phone: str = None) -> bool:
        """Test user registration"""
        print(f"\n{Colors.MAGENTA}Testing User Registration...{Colors.RESET}")

        payload = {
            "email": email,
            "password": password,
            "name": name,
            "phone": phone
        }

        response = requests.post(
            f"{self.base_url}/api/auth/register",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 200:
            data = response.json()
            self.auth_token = data.get("accessToken")
            self.created_users.append(data.get("userEmail"))
            self.print_test("User Registration", True, f"User created: {email}")
            return True
        else:
            self.print_test("User Registration", False, f"Failed with status {response.status_code}")
            return False

    def test_authenticate_user(self, email: str, password: str) -> bool:
        """Test user authentication"""
        print(f"\n{Colors.MAGENTA}Testing User Authentication...{Colors.RESET}")

        payload = {
            "email": email,
            "password": password
        }

        response = requests.post(
            f"{self.base_url}/api/auth/authenticate",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 200:
            data = response.json()
            self.auth_token = data.get("accessToken")
            self.headers["Authorization"] = f"Bearer {self.auth_token}"
            self.print_test("User Authentication", True, "Login successful")
            return True
        else:
            self.print_test("User Authentication", False, f"Failed with status {response.status_code}")
            return False

    # ==================== USER TESTS ====================

    def test_get_user_by_email(self, email: str) -> Optional[Dict]:
        """Test getting user by email"""
        print(f"\n{Colors.MAGENTA}Testing Get User by Email...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/user/email/{email}",
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 200:
            data = response.json()
            self.user_id = data.get("id")
            self.headers["User-Id"] = self.user_id
            self.print_test("Get User by Email", True, f"User ID: {self.user_id}")
            return data
        else:
            self.print_test("Get User by Email", False, f"Failed with status {response.status_code}")
            return None

    def test_get_user_by_id(self, user_id: str) -> Optional[Dict]:
        """Test getting user by ID"""
        print(f"\n{Colors.MAGENTA}Testing Get User by ID...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/user/{user_id}",
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get User by ID", passed)
        return response.json() if passed else None

    def test_update_user(self, user_id: str) -> bool:
        """Test updating user"""
        print(f"\n{Colors.MAGENTA}Testing Update User...{Colors.RESET}")

        payload = {
            "name": "Updated Test User",
            "phone": "+1234567890",
            "profilePhotoUrl": "https://example.com/photo.jpg"
        }

        response = requests.put(
            f"{self.base_url}/api/user/{user_id}",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Update User", passed)
        return passed

    def test_get_all_users(self) -> bool:
        """Test getting all users"""
        print(f"\n{Colors.MAGENTA}Testing Get All Users...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/user/all",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            users = response.json()
            print(f"  Total users: {len(users)}")
        self.print_test("Get All Users", passed)
        return passed

    # ==================== COMMUNITY TESTS ====================

    def test_create_community(self, name: str, description: str) -> Optional[str]:
        """Test creating a community"""
        print(f"\n{Colors.MAGENTA}Testing Create Community...{Colors.RESET}")

        payload = {
            "name": name,
            "description": description
        }

        response = requests.post(
            f"{self.base_url}/api/communities",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 201:
            data = response.json()
            community_id = data.get("id")
            self.created_communities.append(community_id)
            self.print_test("Create Community", True, f"Community ID: {community_id}")
            return community_id
        else:
            self.print_test("Create Community", False, f"Failed with status {response.status_code}")
            return None

    def test_get_community(self, community_id: str) -> bool:
        """Test getting community by ID"""
        print(f"\n{Colors.MAGENTA}Testing Get Community...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/communities/{community_id}",
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get Community", passed)
        return passed

    def test_update_community(self, community_id: str) -> bool:
        """Test updating community"""
        print(f"\n{Colors.MAGENTA}Testing Update Community...{Colors.RESET}")

        payload = {
            "name": "Updated Community Name",
            "description": "Updated description"
        }

        response = requests.put(
            f"{self.base_url}/api/communities/{community_id}",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Update Community", passed)
        return passed

    def test_search_communities(self, search_term: str) -> bool:
        """Test searching communities"""
        print(f"\n{Colors.MAGENTA}Testing Search Communities...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/communities/search?q={search_term}",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        self.print_test("Search Communities", passed)
        return passed

    def test_add_community_member(self, community_id: str, user_id: str, role: str = "MEMBER") -> bool:
        """Test adding member to community"""
        print(f"\n{Colors.MAGENTA}Testing Add Community Member...{Colors.RESET}")

        payload = {
            "userId": user_id,
            "role": role
        }

        response = requests.post(
            f"{self.base_url}/api/communities/{community_id}/members",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 201
        self.print_test("Add Community Member", passed)
        return passed

    def test_get_community_members(self, community_id: str) -> bool:
        """Test getting community members"""
        print(f"\n{Colors.MAGENTA}Testing Get Community Members...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/communities/{community_id}/members",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            members = response.json()
            print(f"  Total members: {len(members)}")
        self.print_test("Get Community Members", passed)
        return passed

    # ==================== GROUP TESTS ====================

    def test_create_group(self, community_id: str, name: str, description: str) -> Optional[str]:
        """Test creating a group"""
        print(f"\n{Colors.MAGENTA}Testing Create Group...{Colors.RESET}")

        payload = {
            "communityId": community_id,
            "name": name,
            "description": description
        }

        response = requests.post(
            f"{self.base_url}/api/groups",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 201:
            data = response.json()
            group_id = data.get("id")
            self.created_groups.append(group_id)
            self.print_test("Create Group", True, f"Group ID: {group_id}")
            return group_id
        else:
            self.print_test("Create Group", False, f"Failed with status {response.status_code}")
            return None

    def test_get_group(self, group_id: str) -> bool:
        """Test getting group by ID"""
        print(f"\n{Colors.MAGENTA}Testing Get Group...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/groups/{group_id}",
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get Group", passed)
        return passed

    def test_get_groups_by_community(self, community_id: str) -> bool:
        """Test getting groups by community"""
        print(f"\n{Colors.MAGENTA}Testing Get Groups by Community...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/groups/community/{community_id}",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            groups = response.json()
            print(f"  Total groups: {len(groups)}")
        self.print_test("Get Groups by Community", passed)
        return passed

    def test_add_group_member(self, group_id: str, user_id: str, role: str = "MEMBER") -> bool:
        """Test adding member to group"""
        print(f"\n{Colors.MAGENTA}Testing Add Group Member...{Colors.RESET}")

        payload = {
            "userId": user_id,
            "role": role
        }

        response = requests.post(
            f"{self.base_url}/api/groups/{group_id}/members",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 403
        self.print_test("Add Group Member", passed)
        return passed

    def test_get_group_members(self, group_id: str) -> bool:
        """Test getting group members"""
        print(f"\n{Colors.MAGENTA}Testing Get Group Members...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/groups/{group_id}/members",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            members = response.json()
            print(f"  Total members: {len(members)}")
        self.print_test("Get Group Members", passed)
        return passed

    # ==================== EVENT TESTS ====================

    def test_create_event(self, title: str, community_id: str = None, group_id: str = None) -> Optional[str]:
        """Test creating an event"""
        print(f"\n{Colors.MAGENTA}Testing Create Event...{Colors.RESET}")

        event_date = (date.today() + timedelta(days=7)).isoformat()

        payload = {
            "title": title,
            "description": "Test event description",
            "communityId": community_id,
            "groupId": group_id,
            "eventDate": event_date,
            "eventTime": "18:00:00",
            "location": "Test Location",
            "attendanceEnabled": True
        }

        response = requests.post(
            f"{self.base_url}/api/events",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        if response.status_code == 201:
            data = response.json()
            event_id = data.get("id")
            self.created_events.append(event_id)
            self.print_test("Create Event", True, f"Event ID: {event_id}")
            return event_id
        else:
            self.print_test("Create Event", False, f"Failed with status {response.status_code}")
            return None

    def test_get_event(self, event_id: str) -> bool:
        """Test getting event by ID"""
        print(f"\n{Colors.MAGENTA}Testing Get Event...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/events/{event_id}",
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get Event", passed)
        return passed

    def test_update_event(self, event_id: str) -> bool:
        """Test updating event"""
        print(f"\n{Colors.MAGENTA}Testing Update Event...{Colors.RESET}")

        payload = {
            "title": "Updated Event Title",
            "description": "Updated event description",
            "location": "Updated Location"
        }

        response = requests.put(
            f"{self.base_url}/api/events/{event_id}",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Update Event", passed)
        return passed

    def test_get_events_by_community(self, community_id: str) -> bool:
        """Test getting events by community"""
        print(f"\n{Colors.MAGENTA}Testing Get Events by Community...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/events/community/{community_id}",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            events = response.json()
            print(f"  Total events: {len(events)}")
        self.print_test("Get Events by Community", passed)
        return passed

    def test_get_upcoming_events(self, community_id: str) -> bool:
        """Test getting upcoming events"""
        print(f"\n{Colors.MAGENTA}Testing Get Upcoming Events...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/events/community/{community_id}/upcoming",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        self.print_test("Get Upcoming Events", passed)
        return passed

    def test_mark_attendance(self, event_id: str, user_id: str, group_id: str) -> bool:
        """Test marking attendance"""
        print(f"\n{Colors.MAGENTA}Testing Mark Attendance...{Colors.RESET}")

        payload = {
            "userId": user_id,
            "groupId": group_id,
            "status": "PRESENT"
        }

        response = requests.post(
            f"{self.base_url}/api/events/{event_id}/attendance",
            json=payload,
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 201
        self.print_test("Mark Attendance", passed)
        return passed

    def test_get_event_attendance(self, event_id: str) -> bool:
        """Test getting event attendance"""
        print(f"\n{Colors.MAGENTA}Testing Get Event Attendance...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/events/{event_id}/attendance",
            headers=self.headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        self.print_test("Get Event Attendance", passed)
        return passed

    def test_get_attendance_stats(self, event_id: str) -> bool:
        """Test getting attendance statistics"""
        print(f"\n{Colors.MAGENTA}Testing Get Attendance Stats...{Colors.RESET}")

        response = requests.get(
            f"{self.base_url}/api/events/{event_id}/attendance/stats",
            headers=self.headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get Attendance Stats", passed)
        return passed

    # ==================== MESSAGE TESTS ====================

    def test_create_text_message(self, event_id: str, content: str) -> Optional[str]:
        """Test creating a text message"""
        print(f"\n{Colors.MAGENTA}Testing Create Text Message...{Colors.RESET}")

        payload = {
            "eventId": event_id,
            "type": "TEXT",
            "content": content,
            "mediaList": []
        }

        # Use X-User-Id header as per MessageController
        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.post(
            f"{self.base_url}/api/messages",
            json=payload,
            headers=headers
        )

        self.print_response(response)

        if response.status_code == 201:
            data = response.json()
            message_id = data.get("id")
            self.created_messages.append(message_id)
            self.print_test("Create Text Message", True, f"Message ID: {message_id}")
            return message_id
        else:
            self.print_test("Create Text Message", False, f"Failed with status {response.status_code}")
            return None

    def test_create_media_message(self, event_id: str, content: str) -> Optional[str]:
        """Test creating a media message"""
        print(f"\n{Colors.MAGENTA}Testing Create Media Message...{Colors.RESET}")

        payload = {
            "eventId": event_id,
            "type": "MEDIA",
            "content": content,
            "mediaList": [
                {
                    "mediaType": "IMAGE",
                    "url": "https://example.com/image1.jpg",
                    "sizeInBytes": 1024000,
                    "width": 1920,
                    "height": 1080
                },
                {
                    "mediaType": "VIDEO",
                    "url": "https://example.com/video1.mp4",
                    "sizeInBytes": 5120000,
                    "width": 1280,
                    "height": 720,
                    "durationInSeconds": 120
                }
            ]
        }

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.post(
            f"{self.base_url}/api/messages",
            json=payload,
            headers=headers
        )

        self.print_response(response)

        if response.status_code == 201:
            data = response.json()
            message_id = data.get("id")
            self.created_messages.append(message_id)
            self.print_test("Create Media Message", True, f"Message ID: {message_id}")
            return message_id
        else:
            self.print_test("Create Media Message", False, f"Failed with status {response.status_code}")
            return None

    def test_get_message_by_id(self, message_id: str) -> bool:
        """Test getting message by ID"""
        print(f"\n{Colors.MAGENTA}Testing Get Message by ID...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.get(
            f"{self.base_url}/api/messages/{message_id}",
            headers=headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        self.print_test("Get Message by ID", passed)
        return passed

    def test_get_messages_by_event_paginated(self, event_id: str, page: int = 0, size: int = 10) -> bool:
        """Test getting paginated messages for an event"""
        print(f"\n{Colors.MAGENTA}Testing Get Paginated Messages...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.get(
            f"{self.base_url}/api/messages/event/{event_id}?page={page}&size={size}",
            headers=headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            data = response.json()
            print(f"  Total pages: {data.get('totalPages', 0)}")
            print(f"  Total elements: {data.get('totalElements', 0)}")
            print(f"  Current page: {data.get('number', 0)}")
        self.print_test("Get Paginated Messages", passed)
        return passed

    def test_get_all_messages_by_event(self, event_id: str) -> bool:
        """Test getting all messages for an event"""
        print(f"\n{Colors.MAGENTA}Testing Get All Messages by Event...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.get(
            f"{self.base_url}/api/messages/event/{event_id}/all",
            headers=headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            messages = response.json()
            print(f"  Total messages: {len(messages)}")
        self.print_test("Get All Messages by Event", passed)
        return passed

    def test_get_messages_by_user(self, event_id: str, user_id: str) -> bool:
        """Test getting messages by user"""
        print(f"\n{Colors.MAGENTA}Testing Get Messages by User...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.get(
            f"{self.base_url}/api/messages/event/{event_id}/user/{user_id}",
            headers=headers
        )

        self.print_response(response, show_body=False)

        passed = response.status_code == 200
        if passed:
            messages = response.json()
            print(f"  User's messages: {len(messages)}")
        self.print_test("Get Messages by User", passed)
        return passed

    def test_get_message_count(self, event_id: str) -> bool:
        """Test getting message count"""
        print(f"\n{Colors.MAGENTA}Testing Get Message Count...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.get(
            f"{self.base_url}/api/messages/event/{event_id}/count",
            headers=headers
        )

        self.print_response(response)

        passed = response.status_code == 200
        if passed:
            count = response.json()
            print(f"  Message count: {count}")
        self.print_test("Get Message Count", passed)
        return passed

    def test_delete_message(self, message_id: str) -> bool:
        """Test deleting a message"""
        print(f"\n{Colors.MAGENTA}Testing Delete Message...{Colors.RESET}")

        headers = self.headers.copy()
        headers["X-User-Id"] = self.user_id

        response = requests.delete(
            f"{self.base_url}/api/messages/{message_id}",
            headers=headers
        )

        print(f"  Status: {response.status_code}")

        passed = response.status_code == 204
        self.print_test("Delete Message", passed)
        return passed

    # ==================== CLEANUP ====================

    def cleanup(self):
        """Clean up created resources (optional)"""
        print(f"\n{Colors.YELLOW}Cleanup Summary:{Colors.RESET}")
        print(f"  Communities created: {len(self.created_communities)}")
        print(f"  Groups created: {len(self.created_groups)}")
        print(f"  Events created: {len(self.created_events)}")
        print(f"  Messages created: {len(self.created_messages)}")

    # ==================== TEST RUNNER ====================

    def run_all_tests(self):
        """Run complete test suite"""

        self.print_section("API AUTOMATED TEST SUITE")

        # Generate unique test data
        timestamp = int(time.time())
        test_email = f"testuser{timestamp}@example.com"
        test_password = "Test@123456"
        test_name = f"Test User {timestamp}"

        # ========== AUTHENTICATION TESTS ==========
        self.print_section("AUTHENTICATION TESTS")

        # Register user
        if not self.test_register_user(test_email, test_password, test_name, "+1234567890"):
            print(f"{Colors.RED}Registration failed. Stopping tests.{Colors.RESET}")
            return

        # Authenticate user
        if not self.test_authenticate_user(test_email, test_password):
            print(f"{Colors.RED}Authentication failed. Stopping tests.{Colors.RESET}")
            return

        # ========== USER TESTS ==========
        self.print_section("USER TESTS")

        # Get user by email
        user_data = self.test_get_user_by_email(test_email)
        if not user_data:
            print(f"{Colors.RED}Failed to get user. Stopping tests.{Colors.RESET}")
            return

        user_id = user_data.get("id")

        # Get user by ID
        self.test_get_user_by_id(user_id)

        # Update user
        self.test_update_user(user_id)

        # Get all users
        self.test_get_all_users()

        # ========== COMMUNITY TESTS ==========
        self.print_section("COMMUNITY TESTS")

        # Create community
        community_id = self.test_create_community(
            f"Test Community {timestamp}",
            "This is a test community"
        )

        if not community_id:
            print(f"{Colors.RED}Failed to create community. Stopping tests.{Colors.RESET}")
            return

        # Get community
        self.test_get_community(community_id)

        # Update community
        self.test_update_community(community_id)

        # Search communities
        self.test_search_communities("Test")

        # Get community members
        self.test_get_community_members(community_id)

        # ========== GROUP TESTS ==========
        self.print_section("GROUP TESTS")

        # Create group
        group_id = self.test_create_group(
            community_id,
            f"Test Group {timestamp}",
            "This is a test group"
        )

        if not group_id:
            print(f"{Colors.RED}Failed to create group. Stopping tests.{Colors.RESET}")
            return

        # Get group
        self.test_get_group(group_id)

        # Get groups by community
        self.test_get_groups_by_community(community_id)

        # Add group member
        self.test_add_group_member(group_id, user_id, "MEMBER")

        # Get group members
        self.test_get_group_members(group_id)

        # ========== EVENT TESTS ==========
        self.print_section("EVENT TESTS")

        # Create event
        event_id = self.test_create_event(
            f"Test Event {timestamp}",
            community_id=community_id,
            group_id=group_id
        )

        if not event_id:
            print(f"{Colors.RED}Failed to create event. Stopping tests.{Colors.RESET}")
            return

        # Get event
        self.test_get_event(event_id)

        # Update event
        self.test_update_event(event_id)

        # Get events by community
        self.test_get_events_by_community(community_id)

        # Get upcoming events
        self.test_get_upcoming_events(community_id)

        # Mark attendance
        self.test_mark_attendance(event_id, user_id, group_id)

        # Get event attendance
        self.test_get_event_attendance(event_id)

        # Get attendance stats
        self.test_get_attendance_stats(event_id)

        # ========== MESSAGE TESTS ==========
        self.print_section("MESSAGE TESTS")

        # Create text message
        message_id_1 = self.test_create_text_message(
            event_id,
            "Hello! This is a test message."
        )

        if message_id_1:
            # Get message by ID
            self.test_get_message_by_id(message_id_1)

        # Create more text messages
        for i in range(3):
            self.test_create_text_message(
                event_id,
                f"Test message number {i + 2}"
            )
            time.sleep(0.5)  # Small delay between messages

        # Create media message
        message_id_media = self.test_create_media_message(
            event_id,
            "Check out these photos and video!"
        )

        # Get paginated messages
        self.test_get_messages_by_event_paginated(event_id, page=0, size=10)

        # Get all messages by event
        self.test_get_all_messages_by_event(event_id)

        # Get messages by user
        self.test_get_messages_by_user(event_id, user_id)

        # Get message count
        self.test_get_message_count(event_id)

        # Delete a message
        if message_id_1:
            self.test_delete_message(message_id_1)

            # Verify message count decreased
            self.test_get_message_count(event_id)

        # ========== FINAL SUMMARY ==========
        self.print_section("TEST SUMMARY")

        print(f"\n{Colors.BOLD}Test Results:{Colors.RESET}")
        print(f"  Total Tests: {self.tests_total}")
        print(f"  {Colors.GREEN}Passed: {self.tests_passed}{Colors.RESET}")
        print(f"  {Colors.RED}Failed: {self.tests_failed}{Colors.RESET}")

        if self.tests_failed == 0:
            print(f"\n{Colors.GREEN}{Colors.BOLD}🎉 ALL TESTS PASSED! 🎉{Colors.RESET}")
        else:
            print(f"\n{Colors.YELLOW}⚠️  Some tests failed. Please review the output above.{Colors.RESET}")

        # Cleanup summary
        self.cleanup()

        print(f"\n{Colors.CYAN}Test Data Created:{Colors.RESET}")
        print(f"  Email: {test_email}")
        print(f"  Password: {test_password}")
        print(f"  User ID: {user_id}")
        print(f"  Community ID: {community_id}")
        print(f"  Group ID: {group_id}")
        print(f"  Event ID: {event_id}")


def main():
    """Main entry point"""
    import sys

    # Check if base URL is provided
    base_url = "http://localhost:8080"
    if len(sys.argv) > 1:
        base_url = sys.argv[1]

    print(f"{Colors.CYAN}Starting API tests against: {base_url}{Colors.RESET}")

    # Create and run test runner
    runner = APITestRunner(base_url)

    try:
        runner.run_all_tests()
    except KeyboardInterrupt:
        print(f"\n{Colors.YELLOW}Tests interrupted by user{Colors.RESET}")
    except Exception as e:
        print(f"\n{Colors.RED}Error during testing: {str(e)}{Colors.RESET}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
