begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|mapreduce
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|Inet6Address
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|InetAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|UnknownHostException
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|naming
operator|.
name|NamingException
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
name|testclassification
operator|.
name|SmallTests
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
block|{
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestTableInputFormatBase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testTableInputFormatBaseReverseDNSForIPv6
parameter_list|()
throws|throws
name|UnknownHostException
throws|,
name|NamingException
block|{
name|String
name|address
init|=
literal|"ipv6.google.com"
decl_stmt|;
name|String
name|localhost
init|=
literal|null
decl_stmt|;
name|InetAddress
name|addr
init|=
literal|null
decl_stmt|;
name|TableInputFormat
name|inputFormat
init|=
operator|new
name|TableInputFormat
argument_list|()
decl_stmt|;
try|try
block|{
name|localhost
operator|=
name|InetAddress
operator|.
name|getByName
argument_list|(
name|address
argument_list|)
operator|.
name|getCanonicalHostName
argument_list|()
expr_stmt|;
name|addr
operator|=
name|Inet6Address
operator|.
name|getByName
argument_list|(
name|address
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnknownHostException
name|e
parameter_list|)
block|{
comment|// google.com is down, we can probably forgive this test.
return|return;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Should retrun the hostname for this host "
operator|+
name|localhost
operator|+
literal|" addr : "
operator|+
name|addr
argument_list|)
expr_stmt|;
name|String
name|actualHostName
init|=
name|inputFormat
operator|.
name|reverseDNS
argument_list|(
name|addr
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should retrun the hostname for this host. Expected : "
operator|+
name|localhost
operator|+
literal|" Actual : "
operator|+
name|actualHostName
argument_list|,
name|localhost
argument_list|,
name|actualHostName
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

