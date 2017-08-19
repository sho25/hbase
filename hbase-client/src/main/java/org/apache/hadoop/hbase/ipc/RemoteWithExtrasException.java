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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Constructor
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
name|DoNotRetryIOException
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
name|HBaseConfiguration
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|DynamicClassLoader
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**  * A {@link RemoteException} with some extra information.  If source exception  * was a {@link org.apache.hadoop.hbase.DoNotRetryIOException},   * {@link #isDoNotRetry()} will return true.  *<p>A {@link RemoteException} hosts exceptions we got from the server.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED"
argument_list|,
name|justification
operator|=
literal|"None. Address sometime."
argument_list|)
specifier|public
class|class
name|RemoteWithExtrasException
extends|extends
name|RemoteException
block|{
specifier|private
specifier|final
name|String
name|hostname
decl_stmt|;
specifier|private
specifier|final
name|int
name|port
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|doNotRetry
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|ClassLoader
name|CLASS_LOADER
decl_stmt|;
static|static
block|{
name|ClassLoader
name|parent
init|=
name|RemoteWithExtrasException
operator|.
name|class
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|CLASS_LOADER
operator|=
operator|new
name|DynamicClassLoader
argument_list|(
name|conf
argument_list|,
name|parent
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RemoteWithExtrasException
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|msg
parameter_list|,
specifier|final
name|boolean
name|doNotRetry
parameter_list|)
block|{
name|this
argument_list|(
name|className
argument_list|,
name|msg
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|,
name|doNotRetry
argument_list|)
expr_stmt|;
block|}
specifier|public
name|RemoteWithExtrasException
parameter_list|(
name|String
name|className
parameter_list|,
name|String
name|msg
parameter_list|,
specifier|final
name|String
name|hostname
parameter_list|,
specifier|final
name|int
name|port
parameter_list|,
specifier|final
name|boolean
name|doNotRetry
parameter_list|)
block|{
name|super
argument_list|(
name|className
argument_list|,
name|msg
argument_list|)
expr_stmt|;
name|this
operator|.
name|hostname
operator|=
name|hostname
expr_stmt|;
name|this
operator|.
name|port
operator|=
name|port
expr_stmt|;
name|this
operator|.
name|doNotRetry
operator|=
name|doNotRetry
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|IOException
name|unwrapRemoteException
parameter_list|()
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|realClass
decl_stmt|;
try|try
block|{
comment|// try to load a exception class from where the HBase classes are loaded or from Dynamic
comment|// classloader.
name|realClass
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|getClassName
argument_list|()
argument_list|,
literal|false
argument_list|,
name|CLASS_LOADER
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
try|try
block|{
comment|// cause could be a hadoop exception, try to load from hadoop classpath
name|realClass
operator|=
name|Class
operator|.
name|forName
argument_list|(
name|getClassName
argument_list|()
argument_list|,
literal|false
argument_list|,
name|super
operator|.
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
return|return
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Unable to load exception received from server:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|this
argument_list|)
return|;
block|}
block|}
try|try
block|{
return|return
name|instantiateException
argument_list|(
name|realClass
operator|.
name|asSubclass
argument_list|(
name|IOException
operator|.
name|class
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|new
name|DoNotRetryIOException
argument_list|(
literal|"Unable to instantiate exception received from server:"
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|this
argument_list|)
return|;
block|}
block|}
specifier|private
name|IOException
name|instantiateException
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|IOException
argument_list|>
name|cls
parameter_list|)
throws|throws
name|Exception
block|{
name|Constructor
argument_list|<
name|?
extends|extends
name|IOException
argument_list|>
name|cn
init|=
name|cls
operator|.
name|getConstructor
argument_list|(
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|cn
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|IOException
name|ex
init|=
name|cn
operator|.
name|newInstance
argument_list|(
name|this
operator|.
name|getMessage
argument_list|()
argument_list|)
decl_stmt|;
name|ex
operator|.
name|initCause
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|ex
return|;
block|}
comment|/**    * @return null if not set    */
specifier|public
name|String
name|getHostname
parameter_list|()
block|{
return|return
name|this
operator|.
name|hostname
return|;
block|}
comment|/**    * @return -1 if not set    */
specifier|public
name|int
name|getPort
parameter_list|()
block|{
return|return
name|this
operator|.
name|port
return|;
block|}
comment|/**    * @return True if origin exception was a do not retry type.    */
specifier|public
name|boolean
name|isDoNotRetry
parameter_list|()
block|{
return|return
name|this
operator|.
name|doNotRetry
return|;
block|}
block|}
end_class

end_unit

