begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertFalse
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotSame
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Addressing
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestServerName
block|{
annotation|@
name|Test
specifier|public
name|void
name|testRegexPatterns
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|Pattern
operator|.
name|matches
argument_list|(
name|Addressing
operator|.
name|VALID_PORT_REGEX
argument_list|,
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|Pattern
operator|.
name|matches
argument_list|(
name|Addressing
operator|.
name|VALID_PORT_REGEX
argument_list|,
literal|""
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ServerName
operator|.
name|SERVERNAME_PATTERN
operator|.
name|matcher
argument_list|(
literal|"www1.example.org,1234,567"
argument_list|)
operator|.
name|matches
argument_list|()
argument_list|)
expr_stmt|;
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"a.b.c,58102,1319771740322"
argument_list|)
expr_stmt|;
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"192.168.1.199,58102,1319771740322"
argument_list|)
expr_stmt|;
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"a.b.c:58102"
argument_list|)
expr_stmt|;
name|ServerName
operator|.
name|parseServerName
argument_list|(
literal|"192.168.1.199:58102"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseOfBytes
parameter_list|()
block|{
specifier|final
name|String
name|snStr
init|=
literal|"www.example.org,1234,5678"
decl_stmt|;
name|ServerName
name|sn
init|=
operator|new
name|ServerName
argument_list|(
name|snStr
argument_list|)
decl_stmt|;
name|byte
index|[]
name|versionedBytes
init|=
name|sn
operator|.
name|getVersionedBytes
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|snStr
argument_list|,
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|versionedBytes
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|hostnamePortStr
init|=
literal|"www.example.org:1234"
decl_stmt|;
name|byte
index|[]
name|bytes
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|hostnamePortStr
argument_list|)
decl_stmt|;
name|String
name|expecting
init|=
name|hostnamePortStr
operator|.
name|replace
argument_list|(
literal|":"
argument_list|,
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
argument_list|)
operator|+
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
operator|+
name|ServerName
operator|.
name|NON_STARTCODE
decl_stmt|;
name|assertEquals
argument_list|(
name|expecting
argument_list|,
name|ServerName
operator|.
name|parseVersionedServerName
argument_list|(
name|bytes
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testServerName
parameter_list|()
block|{
name|ServerName
name|sn
init|=
operator|new
name|ServerName
argument_list|(
literal|"www.example.org"
argument_list|,
literal|1234
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|ServerName
name|sn2
init|=
operator|new
name|ServerName
argument_list|(
literal|"www.example.org"
argument_list|,
literal|1234
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|ServerName
name|sn3
init|=
operator|new
name|ServerName
argument_list|(
literal|"www.example.org"
argument_list|,
literal|1234
argument_list|,
literal|56789
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|sn
operator|.
name|equals
argument_list|(
name|sn2
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|sn
operator|.
name|equals
argument_list|(
name|sn3
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sn
operator|.
name|hashCode
argument_list|()
argument_list|,
name|sn2
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
name|sn
operator|.
name|hashCode
argument_list|()
argument_list|,
name|sn3
operator|.
name|hashCode
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|,
name|ServerName
operator|.
name|getServerName
argument_list|(
literal|"www.example.org"
argument_list|,
literal|1234
argument_list|,
literal|5678
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|,
name|ServerName
operator|.
name|getServerName
argument_list|(
literal|"www.example.org:1234"
argument_list|,
literal|5678
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|,
literal|"www.example.org"
operator|+
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
operator|+
literal|"1234"
operator|+
name|ServerName
operator|.
name|SERVERNAME_SEPARATOR
operator|+
literal|"5678"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|getServerStartcodeFromServerName
parameter_list|()
block|{
name|ServerName
name|sn
init|=
operator|new
name|ServerName
argument_list|(
literal|"www.example.org"
argument_list|,
literal|1234
argument_list|,
literal|5678
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|5678
argument_list|,
name|ServerName
operator|.
name|getServerStartcodeFromServerName
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotSame
argument_list|(
literal|5677
argument_list|,
name|ServerName
operator|.
name|getServerStartcodeFromServerName
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|org
operator|.
name|junit
operator|.
name|Rule
specifier|public
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
name|cu
init|=
operator|new
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ResourceCheckerJUnitRule
argument_list|()
decl_stmt|;
block|}
end_class

end_unit

