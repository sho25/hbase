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
name|ipc
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|CellScannable
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
name|CellScanner
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

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
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
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Factory to create a {@link HBaseRpcController}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RpcControllerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RpcControllerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Custom RPC Controller factory allows frameworks to change the RPC controller. If the configured    * controller cannot be found in the classpath or loaded, we fall back to the default RPC    * controller factory.    */
specifier|public
specifier|static
specifier|final
name|String
name|CUSTOM_CONTROLLER_CONF_KEY
init|=
literal|"hbase.rpc.controllerfactory.class"
decl_stmt|;
specifier|protected
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|public
name|RpcControllerFactory
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
specifier|public
name|HBaseRpcController
name|newController
parameter_list|()
block|{
comment|// TODO: Set HConstants default rpc timeout here rather than nothing?
return|return
operator|new
name|HBaseRpcControllerImpl
argument_list|()
return|;
block|}
specifier|public
name|HBaseRpcController
name|newController
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
return|return
operator|new
name|HBaseRpcControllerImpl
argument_list|(
name|cellScanner
argument_list|)
return|;
block|}
specifier|public
name|HBaseRpcController
name|newController
parameter_list|(
specifier|final
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cellIterables
parameter_list|)
block|{
return|return
operator|new
name|HBaseRpcControllerImpl
argument_list|(
name|cellIterables
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|RpcControllerFactory
name|instantiate
parameter_list|(
name|Configuration
name|configuration
parameter_list|)
block|{
name|String
name|rpcControllerFactoryClazz
init|=
name|configuration
operator|.
name|get
argument_list|(
name|CUSTOM_CONTROLLER_CONF_KEY
argument_list|,
name|RpcControllerFactory
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|ReflectionUtils
operator|.
name|instantiateWithCustomCtor
argument_list|(
name|rpcControllerFactoryClazz
argument_list|,
operator|new
name|Class
index|[]
block|{
name|Configuration
operator|.
name|class
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
name|configuration
block|}
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
decl||
name|NoClassDefFoundError
name|ex
parameter_list|)
block|{
comment|// HBASE-14960: In case the RPCController is in a non-HBase jar (Phoenix), but the application
comment|// is a pure HBase application, we want to fallback to the default one.
name|String
name|msg
init|=
literal|"Cannot load configured \""
operator|+
name|CUSTOM_CONTROLLER_CONF_KEY
operator|+
literal|"\" ("
operator|+
name|rpcControllerFactoryClazz
operator|+
literal|") from hbase-site.xml, falling back to use "
operator|+
literal|"default RpcControllerFactory"
decl_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|,
name|ex
argument_list|)
expr_stmt|;
comment|// if DEBUG enabled, we want the exception, but still log in WARN level
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|RpcControllerFactory
argument_list|(
name|configuration
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

