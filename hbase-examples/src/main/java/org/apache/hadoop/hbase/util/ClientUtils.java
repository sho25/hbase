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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|Subject
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|AppConfigurationEntry
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginContext
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|security
operator|.
name|auth
operator|.
name|login
operator|.
name|LoginException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang3
operator|.
name|StringUtils
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
name|thrift
operator|.
name|generated
operator|.
name|TCell
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
name|thrift
operator|.
name|generated
operator|.
name|TRowResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Common Utility class for clients  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|ClientUtils
block|{
specifier|private
name|ClientUtils
parameter_list|()
block|{
comment|// Empty block
block|}
comment|/**    * To authenticate the demo client, kinit should be invoked ahead. Here we try to get the    * Kerberos credential from the ticket cache    *    * @return LoginContext Object    * @throws LoginException Exception thrown if unable to get LoginContext    */
specifier|public
specifier|static
name|LoginContext
name|getLoginContext
parameter_list|()
throws|throws
name|LoginException
block|{
return|return
operator|new
name|LoginContext
argument_list|(
name|StringUtils
operator|.
name|EMPTY
argument_list|,
operator|new
name|Subject
argument_list|()
argument_list|,
literal|null
argument_list|,
operator|new
name|Configuration
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|AppConfigurationEntry
index|[]
name|getAppConfigurationEntry
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|options
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"useKeyTab"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"storeKey"
argument_list|,
literal|"false"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"doNotPrompt"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"useTicketCache"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"renewTGT"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"refreshKrb5Config"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|options
operator|.
name|put
argument_list|(
literal|"isInitiator"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
name|String
name|ticketCache
init|=
name|System
operator|.
name|getenv
argument_list|(
literal|"KRB5CCNAME"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ticketCache
operator|!=
literal|null
condition|)
block|{
name|options
operator|.
name|put
argument_list|(
literal|"ticketCache"
argument_list|,
name|ticketCache
argument_list|)
expr_stmt|;
block|}
name|options
operator|.
name|put
argument_list|(
literal|"debug"
argument_list|,
literal|"true"
argument_list|)
expr_stmt|;
return|return
operator|new
name|AppConfigurationEntry
index|[]
block|{
operator|new
name|AppConfigurationEntry
argument_list|(
literal|"com.sun.security.auth.module.Krb5LoginModule"
argument_list|,
name|AppConfigurationEntry
operator|.
name|LoginModuleControlFlag
operator|.
name|REQUIRED
argument_list|,
name|options
argument_list|)
block|}
return|;
block|}
block|}
argument_list|)
return|;
block|}
comment|/**    * copy values into a TreeMap to get them in sorted order and print it    *    * @param rowResult Holds row name and then a map of columns to cells    */
specifier|public
specifier|static
name|void
name|printRow
parameter_list|(
specifier|final
name|TRowResult
name|rowResult
parameter_list|)
block|{
name|TreeMap
argument_list|<
name|String
argument_list|,
name|TCell
argument_list|>
name|sorted
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|ByteBuffer
argument_list|,
name|TCell
argument_list|>
name|column
range|:
name|rowResult
operator|.
name|columns
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sorted
operator|.
name|put
argument_list|(
name|utf8
argument_list|(
name|column
operator|.
name|getKey
argument_list|()
operator|.
name|array
argument_list|()
argument_list|)
argument_list|,
name|column
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|StringBuilder
name|rowStr
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|SortedMap
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|TCell
argument_list|>
name|entry
range|:
name|sorted
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|rowStr
operator|.
name|append
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|" => "
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
name|utf8
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|value
operator|.
name|array
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|rowStr
operator|.
name|append
argument_list|(
literal|"; "
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"row: "
operator|+
name|utf8
argument_list|(
name|rowResult
operator|.
name|row
operator|.
name|array
argument_list|()
argument_list|)
operator|+
literal|", cols: "
operator|+
name|rowStr
argument_list|)
expr_stmt|;
block|}
comment|/**    * Helper to translate byte[]'s to UTF8 strings    *    * @param buf byte array buffer    * @return UTF8 decoded string value    */
specifier|public
specifier|static
name|String
name|utf8
parameter_list|(
specifier|final
name|byte
index|[]
name|buf
parameter_list|)
block|{
try|try
block|{
return|return
name|Bytes
operator|.
name|toString
argument_list|(
name|buf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
return|return
literal|"[INVALID UTF-8]"
return|;
block|}
block|}
block|}
end_class

end_unit

