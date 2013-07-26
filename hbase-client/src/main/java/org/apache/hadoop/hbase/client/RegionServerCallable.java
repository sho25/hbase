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
name|java
operator|.
name|net
operator|.
name|ConnectException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|SocketTimeoutException
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|classification
operator|.
name|InterfaceStability
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
name|exceptions
operator|.
name|NotServingRegionException
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
name|exceptions
operator|.
name|RegionMovedException
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
name|protobuf
operator|.
name|generated
operator|.
name|ClientProtos
operator|.
name|ClientService
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
comment|/**  * Implementations call a RegionServer and implement {@link #call()}.  * Passed to a {@link RpcRetryingCaller} so we retry on fail.  * @param<T> the class that the ServerCallable handles  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
specifier|abstract
class|class
name|RegionServerCallable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|RetryingCallable
argument_list|<
name|T
argument_list|>
block|{
comment|// Public because used outside of this package over in ipc.
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionServerCallable
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|HRegionLocation
name|location
decl_stmt|;
specifier|private
name|ClientService
operator|.
name|BlockingInterface
name|stub
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
name|RegionServerCallable
parameter_list|(
name|HConnection
name|connection
parameter_list|,
name|byte
index|[]
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
comment|/**    * Prepare for connection to the server hosting region with row from tablename.  Does lookup    * to find region location and hosting server.    * @param reload Set this to true if connection should re-find the region    * @throws IOException e    */
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
name|this
operator|.
name|location
operator|=
name|connection
operator|.
name|getRegionLocation
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|reload
argument_list|)
expr_stmt|;
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
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
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
name|setStub
argument_list|(
name|getConnection
argument_list|()
operator|.
name|getClient
argument_list|(
name|getLocation
argument_list|()
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return {@link HConnection} instance used by this Callable.    */
name|HConnection
name|getConnection
parameter_list|()
block|{
return|return
name|this
operator|.
name|connection
return|;
block|}
specifier|protected
name|ClientService
operator|.
name|BlockingInterface
name|getStub
parameter_list|()
block|{
return|return
name|this
operator|.
name|stub
return|;
block|}
name|void
name|setStub
parameter_list|(
specifier|final
name|ClientService
operator|.
name|BlockingInterface
name|stub
parameter_list|)
block|{
name|this
operator|.
name|stub
operator|=
name|stub
expr_stmt|;
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
name|byte
index|[]
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
name|t
operator|instanceof
name|SocketTimeoutException
operator|||
name|t
operator|instanceof
name|ConnectException
operator|||
name|t
operator|instanceof
name|RetriesExhaustedException
operator|||
operator|(
name|location
operator|!=
literal|null
operator|&&
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
comment|// if thrown these exceptions, we clear all the cache entries that
comment|// map to that slow/dead server; otherwise, let cache miss and ask
comment|// .META. again to find the new location
name|getConnection
argument_list|()
operator|.
name|clearCaches
argument_list|(
name|location
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|RegionMovedException
condition|)
block|{
name|getConnection
argument_list|()
operator|.
name|updateCachedLocations
argument_list|(
name|tableName
argument_list|,
name|row
argument_list|,
name|t
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|instanceof
name|NotServingRegionException
operator|&&
operator|!
name|retrying
condition|)
block|{
comment|// Purge cache entries for this specific region from META cache
comment|// since we don't call connect(true) when number of retries is 1.
name|getConnection
argument_list|()
operator|.
name|deleteCachedRegionLocation
argument_list|(
name|location
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
name|Bytes
operator|.
name|toString
argument_list|(
name|tableName
argument_list|)
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
comment|// Tries hasn't been bumped up yet so we use "tries + 1" to get right pause time
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
operator|+
literal|1
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
block|}
end_class

end_unit

