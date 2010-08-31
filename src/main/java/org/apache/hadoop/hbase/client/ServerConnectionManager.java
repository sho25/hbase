begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ZooKeeperConnectionException
import|;
end_import

begin_comment
comment|/**  * Used by server processes to expose HServerConnection method  * so can call HConnectionManager#setRootRegionLocation  */
end_comment

begin_class
specifier|public
class|class
name|ServerConnectionManager
extends|extends
name|HConnectionManager
block|{
comment|/*    * Not instantiable    */
specifier|private
name|ServerConnectionManager
parameter_list|()
block|{}
comment|/**    * Get the connection object for the instance specified by the configuration    * If no current connection exists, create a new connection for that instance    * @param conf configuration    * @return HConnection object for the instance specified by the configuration    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|ServerConnection
name|getConnection
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
block|{
return|return
operator|(
name|ServerConnection
operator|)
name|HConnectionManager
operator|.
name|getConnection
argument_list|(
name|conf
argument_list|)
return|;
block|}
block|}
end_class

end_unit

