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
name|regionserver
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
name|client
operator|.
name|TableDescriptor
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
comment|/**  * The class that creates a flush policy from a conf and HTableDescriptor.  *<p>  * The default flush policy is {@link FlushLargeStoresPolicy}. And for 0.98, the default flush  * policy is {@link FlushAllStoresPolicy}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
class|class
name|FlushPolicyFactory
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
name|FlushPolicyFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_FLUSH_POLICY_KEY
init|=
literal|"hbase.regionserver.flush.policy"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|FlushPolicy
argument_list|>
name|DEFAULT_FLUSH_POLICY_CLASS
init|=
name|FlushAllLargeStoresPolicy
operator|.
name|class
decl_stmt|;
comment|/**    * Create the FlushPolicy configured for the given table.    */
specifier|public
specifier|static
name|FlushPolicy
name|create
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|Class
argument_list|<
name|?
extends|extends
name|FlushPolicy
argument_list|>
name|clazz
init|=
name|getFlushPolicyClass
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|FlushPolicy
name|policy
init|=
name|ReflectionUtils
operator|.
name|newInstance
argument_list|(
name|clazz
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|policy
operator|.
name|configureForRegion
argument_list|(
name|region
argument_list|)
expr_stmt|;
return|return
name|policy
return|;
block|}
comment|/**    * Get FlushPolicy class for the given table.    */
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|FlushPolicy
argument_list|>
name|getFlushPolicyClass
parameter_list|(
name|TableDescriptor
name|htd
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|className
init|=
name|htd
operator|.
name|getFlushPolicyClassName
argument_list|()
decl_stmt|;
if|if
condition|(
name|className
operator|==
literal|null
condition|)
block|{
name|className
operator|=
name|conf
operator|.
name|get
argument_list|(
name|HBASE_FLUSH_POLICY_KEY
argument_list|,
name|DEFAULT_FLUSH_POLICY_CLASS
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|Class
argument_list|<
name|?
extends|extends
name|FlushPolicy
argument_list|>
name|clazz
init|=
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|FlushPolicy
operator|.
name|class
argument_list|)
decl_stmt|;
return|return
name|clazz
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Unable to load configured flush policy '"
operator|+
name|className
operator|+
literal|"' for table '"
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
operator|+
literal|"', load default flush policy "
operator|+
name|DEFAULT_FLUSH_POLICY_CLASS
operator|.
name|getName
argument_list|()
operator|+
literal|" instead"
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|DEFAULT_FLUSH_POLICY_CLASS
return|;
block|}
block|}
block|}
end_class

end_unit

