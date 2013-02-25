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
name|util
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
name|HBaseTestingUtility
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
name|Server
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
name|exceptions
operator|.
name|ZooKeeperConnectionException
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
name|catalog
operator|.
name|CatalogTracker
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
import|;
end_import

begin_comment
comment|/**  * Basic mock Server for handler tests.  */
end_comment

begin_class
specifier|public
class|class
name|MockServer
implements|implements
name|Server
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MockServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|final
specifier|static
name|ServerName
name|NAME
init|=
operator|new
name|ServerName
argument_list|(
literal|"MockServer"
argument_list|,
literal|123
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
name|boolean
name|stopped
decl_stmt|;
name|boolean
name|aborted
decl_stmt|;
specifier|final
name|ZooKeeperWatcher
name|zk
decl_stmt|;
specifier|final
name|HBaseTestingUtility
name|htu
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|public
name|MockServer
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
comment|// Shutdown default constructor by making it private.
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MockServer
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|htu
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|this
argument_list|(
name|htu
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * @param htu Testing utility to use    * @param zkw If true, create a zkw.    * @throws ZooKeeperConnectionException    * @throws IOException    */
specifier|public
name|MockServer
parameter_list|(
specifier|final
name|HBaseTestingUtility
name|htu
parameter_list|,
specifier|final
name|boolean
name|zkw
parameter_list|)
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|this
operator|.
name|htu
operator|=
name|htu
expr_stmt|;
name|this
operator|.
name|zk
operator|=
name|zkw
condition|?
operator|new
name|ZooKeeperWatcher
argument_list|(
name|htu
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|NAME
operator|.
name|toString
argument_list|()
argument_list|,
name|this
argument_list|,
literal|true
argument_list|)
else|:
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Abort why="
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|stop
argument_list|(
name|why
argument_list|)
expr_stmt|;
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stop why="
operator|+
name|why
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopped
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|this
operator|.
name|htu
operator|.
name|getConfiguration
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|this
operator|.
name|zk
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
name|this
operator|.
name|aborted
return|;
block|}
block|}
end_class

end_unit

