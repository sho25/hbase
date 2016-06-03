#
# Copyright The Apache Software Foundation
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

module Shell
  module Commands
    class ListPeers< Command
      def help
        return <<-EOF
List all replication peer clusters.

  hbase> list_peers
EOF
      end

      def command()
        peers = replication_admin.list_peers

        formatter.header(["PEER_ID", "CLUSTER_KEY", "STATE", "TABLE_CFS"])

        peers.entrySet().each do |e|
          state = replication_admin.get_peer_state(e.key)
          tableCFs = replication_admin.show_peer_tableCFs(e.key)
          formatter.row([ e.key, e.value, state, tableCFs ])
        end

        formatter.footer()
      end
    end
  end
end
