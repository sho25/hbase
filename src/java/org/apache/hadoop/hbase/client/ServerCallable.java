begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|ipc
operator|.
name|HRegionInterface
import|;
end_import

begin_comment
comment|/**  * Abstract class that implemetns Callable, used by retryable actions.  * @param<T> the class that the ServerCallable handles  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|ServerCallable
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Callable
argument_list|<
name|T
argument_list|>
block|{
specifier|protected
specifier|final
name|HConnection
name|connection
decl_stmt|;
specifier|protected
specifier|final
name|byte
index|[]
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
name|HRegionInterface
name|server
decl_stmt|;
comment|/**    * @param connection    * @param tableName    * @param row    */
specifier|public
name|ServerCallable
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
comment|/**    *     * @param reload set this to true if connection should re-find the region    * @throws IOException    */
specifier|public
name|void
name|instantiateServer
parameter_list|(
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
name|this
operator|.
name|server
operator|=
name|connection
operator|.
name|getHRegionConnection
argument_list|(
name|location
operator|.
name|getServerAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** @return the server name */
specifier|public
name|String
name|getServerName
parameter_list|()
block|{
if|if
condition|(
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
name|location
operator|.
name|getServerAddress
argument_list|()
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/** @return the region name */
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
if|if
condition|(
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
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
return|;
block|}
comment|/** @return the row */
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
block|}
end_class

end_unit

