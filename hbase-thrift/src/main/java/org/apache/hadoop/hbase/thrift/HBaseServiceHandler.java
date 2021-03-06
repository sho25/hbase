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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|HBaseInterfaceAudience
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|Table
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
name|security
operator|.
name|UserProvider
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ConnectionCache
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
comment|/**  * abstract class for HBase handler  * providing a Connection cache and get table/admin method  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|TOOLS
argument_list|)
specifier|public
specifier|abstract
class|class
name|HBaseServiceHandler
block|{
specifier|public
specifier|static
specifier|final
name|String
name|CLEANUP_INTERVAL
init|=
literal|"hbase.thrift.connection.cleanup-interval"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|MAX_IDLETIME
init|=
literal|"hbase.thrift.connection.max-idletime"
decl_stmt|;
specifier|protected
name|Configuration
name|conf
decl_stmt|;
specifier|protected
specifier|final
name|ConnectionCache
name|connectionCache
decl_stmt|;
specifier|public
name|HBaseServiceHandler
parameter_list|(
specifier|final
name|Configuration
name|c
parameter_list|,
specifier|final
name|UserProvider
name|userProvider
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|conf
operator|=
name|c
expr_stmt|;
name|int
name|cleanInterval
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|CLEANUP_INTERVAL
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|int
name|maxIdleTime
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|MAX_IDLETIME
argument_list|,
literal|10
operator|*
literal|60
operator|*
literal|1000
argument_list|)
decl_stmt|;
name|connectionCache
operator|=
operator|new
name|ConnectionCache
argument_list|(
name|conf
argument_list|,
name|userProvider
argument_list|,
name|cleanInterval
argument_list|,
name|maxIdleTime
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|ThriftMetrics
name|metrics
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|initMetrics
parameter_list|(
name|ThriftMetrics
name|metrics
parameter_list|)
block|{
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
block|}
specifier|public
name|void
name|setEffectiveUser
parameter_list|(
name|String
name|effectiveUser
parameter_list|)
block|{
name|connectionCache
operator|.
name|setEffectiveUser
argument_list|(
name|effectiveUser
argument_list|)
expr_stmt|;
block|}
comment|/**    * Obtain HBaseAdmin. Creates the instance if it is not already created.    */
specifier|protected
name|Admin
name|getAdmin
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|connectionCache
operator|.
name|getAdmin
argument_list|()
return|;
block|}
comment|/**    * Creates and returns a Table instance from a given table name.    *    * @param tableName    *          name of table    * @return Table object    * @throws IOException if getting the table fails    */
specifier|protected
name|Table
name|getTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|table
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
return|return
name|connectionCache
operator|.
name|getTable
argument_list|(
name|table
argument_list|)
return|;
block|}
specifier|protected
name|Table
name|getTable
parameter_list|(
specifier|final
name|ByteBuffer
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|getTable
argument_list|(
name|Bytes
operator|.
name|getBytes
argument_list|(
name|tableName
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

