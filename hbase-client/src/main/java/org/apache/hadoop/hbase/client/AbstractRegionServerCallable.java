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
operator|.
name|client
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HRegionInfo
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
name|HRegionLocation
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
name|ServerName
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
name|TableName
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
name|TableNotEnabledException
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
name|classification
operator|.
name|InterfaceAudience
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

begin_comment
comment|/**  * Added by HBASE-15745 Refactor of RPC classes to better accept async changes.  * Temporary.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|AbstractRegionServerCallable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|RetryingCallable
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
specifier|final
name|Connection
name|connection
decl_stmt|;
specifier|protected
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|protected
name|HRegionLocation
name|location
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|int
name|MIN_WAIT_DEAD_SERVER
init|=
literal|10000
decl_stmt|;
comment|/**    * @param connection Connection to use.    * @param tableName Table name to which<code>row</code> belongs.    * @param row The row we want in<code>tableName</code>.    */
specifier|public
name|AbstractRegionServerCallable
parameter_list|(
name|Connection
name|connection
parameter_list|,
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
comment|/**    * @return {@link ClusterConnection} instance used by this Callable.    */
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
operator|(
name|ClusterConnection
operator|)
name|this
operator|.
name|connection
return|;
block|}
specifier|protected
name|HRegionLocation
name|getLocation
parameter_list|()
block|{
return|return
name|this
operator|.
name|location
return|;
block|}
specifier|protected
name|void
name|setLocation
parameter_list|(
specifier|final
name|HRegionLocation
name|location
parameter_list|)
block|{
name|this
operator|.
name|location
operator|=
name|location
expr_stmt|;
block|}
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|this
operator|.
name|tableName
return|;
block|}
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|throwable
parameter_list|(
name|Throwable
name|t
parameter_list|,
name|boolean
name|retrying
parameter_list|)
block|{
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|getConnection
argument_list|()
operator|.
name|updateCachedLocations
argument_list|(
name|tableName
argument_list|,
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|row
argument_list|,
name|t
argument_list|,
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|String
name|getExceptionMessageAdditionalDetail
parameter_list|()
block|{
return|return
literal|"row '"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
operator|+
literal|"' on table '"
operator|+
name|tableName
operator|+
literal|"' at "
operator|+
name|location
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|sleep
parameter_list|(
name|long
name|pause
parameter_list|,
name|int
name|tries
parameter_list|)
block|{
name|long
name|sleep
init|=
name|ConnectionUtils
operator|.
name|getPauseTime
argument_list|(
name|pause
argument_list|,
name|tries
argument_list|)
decl_stmt|;
if|if
condition|(
name|sleep
operator|<
name|MIN_WAIT_DEAD_SERVER
operator|&&
operator|(
name|location
operator|==
literal|null
operator|||
name|getConnection
argument_list|()
operator|.
name|isDeadServer
argument_list|(
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
operator|)
condition|)
block|{
name|sleep
operator|=
name|ConnectionUtils
operator|.
name|addJitter
argument_list|(
name|MIN_WAIT_DEAD_SERVER
argument_list|,
literal|0.10f
argument_list|)
expr_stmt|;
block|}
return|return
name|sleep
return|;
block|}
comment|/**    * @return the HRegionInfo for the current region    */
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|location
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|this
operator|.
name|location
operator|.
name|getRegionInfo
argument_list|()
return|;
block|}
comment|/**    * Prepare for connection to the server hosting region with row from tablename.  Does lookup    * to find region location and hosting server.    * @param reload Set to true to re-check the table state    * @throws IOException e    */
annotation|@
name|Override
specifier|public
name|void
name|prepare
parameter_list|(
specifier|final
name|boolean
name|reload
parameter_list|)
throws|throws
name|IOException
block|{
comment|// check table state if this is a retry
if|if
condition|(
name|reload
operator|&&
operator|!
name|tableName
operator|.
name|equals
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|&&
name|getConnection
argument_list|()
operator|.
name|isTableDisabled
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|TableNotEnabledException
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
operator|+
literal|" is disabled."
argument_list|)
throw|;
block|}
try|try
init|(
name|RegionLocator
name|regionLocator
init|=
name|connection
operator|.
name|getRegionLocator
argument_list|(
name|tableName
argument_list|)
init|)
block|{
name|this
operator|.
name|location
operator|=
name|regionLocator
operator|.
name|getRegionLocation
argument_list|(
name|row
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|location
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Failed to find location, tableName="
operator|+
name|tableName
operator|+
literal|", row="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
operator|+
literal|", reload="
operator|+
name|reload
argument_list|)
throw|;
block|}
name|setClientByServiceName
argument_list|(
name|this
operator|.
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the Rpc client for Client services    * @param serviceName to get client for    * @throws IOException When client could not be created    */
specifier|abstract
name|void
name|setClientByServiceName
parameter_list|(
name|ServerName
name|serviceName
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

