begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|security
operator|.
name|visibility
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
name|util
operator|.
name|ReflectionUtils
import|;
end_import

begin_comment
comment|/**  * Manages singleton instance of {@link VisibilityLabelService}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VisibilityLabelServiceManager
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
name|VisibilityLabelServiceManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VISIBILITY_LABEL_SERVICE_CLASS
init|=
literal|"hbase.regionserver.visibility.label.service.class"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|VisibilityLabelServiceManager
name|INSTANCE
init|=
operator|new
name|VisibilityLabelServiceManager
argument_list|()
decl_stmt|;
specifier|private
specifier|volatile
name|VisibilityLabelService
name|visibilityLabelService
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|vlsClazzName
init|=
literal|null
decl_stmt|;
specifier|private
name|VisibilityLabelServiceManager
parameter_list|()
block|{    }
specifier|public
specifier|static
name|VisibilityLabelServiceManager
name|getInstance
parameter_list|()
block|{
return|return
name|INSTANCE
return|;
block|}
comment|/**    * @param conf    * @return singleton instance of {@link VisibilityLabelService}. The FQCN of the implementation    *         class can be specified using "hbase.regionserver.visibility.label.service.class".    * @throws IOException When VLS implementation, as specified in conf, can not be loaded.    */
specifier|public
name|VisibilityLabelService
name|getVisibilityLabelService
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|vlsClassName
init|=
name|conf
operator|.
name|get
argument_list|(
name|VISIBILITY_LABEL_SERVICE_CLASS
argument_list|,
name|DefaultVisibilityLabelServiceImpl
operator|.
name|class
operator|.
name|getCanonicalName
argument_list|()
argument_list|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|visibilityLabelService
operator|!=
literal|null
condition|)
block|{
name|checkForClusterLevelSingleConf
argument_list|(
name|vlsClassName
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|visibilityLabelService
return|;
block|}
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|this
operator|.
name|visibilityLabelService
operator|!=
literal|null
condition|)
block|{
name|checkForClusterLevelSingleConf
argument_list|(
name|vlsClassName
argument_list|)
expr_stmt|;
return|return
name|this
operator|.
name|visibilityLabelService
return|;
block|}
name|this
operator|.
name|vlsClazzName
operator|=
name|vlsClassName
expr_stmt|;
try|try
block|{
name|this
operator|.
name|visibilityLabelService
operator|=
operator|(
name|VisibilityLabelService
operator|)
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|vlsClassName
argument_list|)
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|visibilityLabelService
return|;
block|}
block|}
specifier|private
name|void
name|checkForClusterLevelSingleConf
parameter_list|(
name|String
name|vlsClassName
parameter_list|)
block|{
assert|assert
name|this
operator|.
name|vlsClazzName
operator|!=
literal|null
assert|;
if|if
condition|(
operator|!
name|this
operator|.
name|vlsClazzName
operator|.
name|equals
argument_list|(
name|vlsClassName
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Trying to use table specific value for config "
operator|+
literal|"'hbase.regionserver.visibility.label.service.class' which is not supported."
operator|+
literal|" Will use the cluster level VisibilityLabelService class "
operator|+
name|this
operator|.
name|vlsClazzName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * @return singleton instance of {@link VisibilityLabelService}.    * @throws IllegalStateException if this called before initialization of singleton instance.    */
specifier|public
name|VisibilityLabelService
name|getVisibilityLabelService
parameter_list|()
block|{
comment|// By the time this method is called, the singleton instance of visibilityLabelService should
comment|// have been created. And it will be created as getVisibilityLabelService(Configuration conf)
comment|// is called from VC#start() and that will be the 1st thing core code do with any CP.
if|if
condition|(
name|this
operator|.
name|visibilityLabelService
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"VisibilityLabelService not yet instantiated"
argument_list|)
throw|;
block|}
return|return
name|this
operator|.
name|visibilityLabelService
return|;
block|}
block|}
end_class

end_unit

