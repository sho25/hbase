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

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * {@link Connection} testing utility.  */
end_comment

begin_class
specifier|public
class|class
name|HConnectionTestingUtility
block|{
comment|/*    * Not part of {@link HBaseTestingUtility} because this class is not in same package as {@link    * ConnectionImplementation}. Would have to reveal ugly {@link ConnectionImplementation} innards    * to HBaseTestingUtility to give it access.    */
comment|/**    * Get a Mocked {@link Connection} that goes with the passed<code>conf</code>    * configuration instance. Minimally the mock will return&lt;code>conf&lt;/conf> when    * {@link Connection#getConfiguration()} is invoked. Be sure to shutdown the    * connection when done by calling {@link Connection#close()} else it will stick around; this is    * probably not what you want.    * @param conf configuration    * @return ConnectionImplementation object for<code>conf</code>    * @throws ZooKeeperConnectionException    */
specifier|public
specifier|static
name|Connection
name|getMockedConnection
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
block|{
name|Connection
name|connection
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|connection
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
return|return
name|connection
return|;
block|}
block|}
end_class

end_unit

