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
operator|.
name|throttle
package|;
end_package

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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|regionserver
operator|.
name|RegionServerServices
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
specifier|final
class|class
name|FlushThroughputControllerFactory
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|FlushThroughputControllerFactory
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_FLUSH_THROUGHPUT_CONTROLLER_KEY
init|=
literal|"hbase.regionserver.flush.throughput.controller"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|ThroughputController
argument_list|>
name|DEFAULT_FLUSH_THROUGHPUT_CONTROLLER_CLASS
init|=
name|NoLimitThroughputController
operator|.
name|class
decl_stmt|;
specifier|private
name|FlushThroughputControllerFactory
parameter_list|()
block|{   }
specifier|public
specifier|static
name|ThroughputController
name|create
parameter_list|(
name|RegionServerServices
name|server
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|Class
argument_list|<
name|?
extends|extends
name|ThroughputController
argument_list|>
name|clazz
init|=
name|getThroughputControllerClass
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ThroughputController
name|controller
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
name|controller
operator|.
name|setup
argument_list|(
name|server
argument_list|)
expr_stmt|;
return|return
name|controller
return|;
block|}
specifier|public
specifier|static
name|Class
argument_list|<
name|?
extends|extends
name|ThroughputController
argument_list|>
name|getThroughputControllerClass
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|className
init|=
name|conf
operator|.
name|get
argument_list|(
name|HBASE_FLUSH_THROUGHPUT_CONTROLLER_KEY
argument_list|,
name|DEFAULT_FLUSH_THROUGHPUT_CONTROLLER_CLASS
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|asSubclass
argument_list|(
name|ThroughputController
operator|.
name|class
argument_list|)
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
literal|"Unable to load configured flush throughput controller '"
operator|+
name|className
operator|+
literal|"', load default throughput controller "
operator|+
name|DEFAULT_FLUSH_THROUGHPUT_CONTROLLER_CLASS
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
name|DEFAULT_FLUSH_THROUGHPUT_CONTROLLER_CLASS
return|;
block|}
block|}
block|}
end_class

end_unit

