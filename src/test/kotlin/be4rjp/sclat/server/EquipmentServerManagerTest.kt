package be4rjp.sclat.server

import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class EquipmentServerManagerTest :
    StringSpec({

        "test addEquipmentCommand" {
            val mockManager = mockk<EquipmentServerManager>(relaxed = true)
            val command = "setting 123456789 123e4567-e89b-12d3-a456-426614174000"

            every { mockManager.addEquipmentCommand(command) } answers {}

            mockManager.addEquipmentCommand(command)

            verify { mockManager.addEquipmentCommand(command) }
        }

        "test handleCommand" {
            val mockManager = mockk<EquipmentServerManager>(relaxed = true)
            val command = "mod testUser"

            every { mockManager.addEquipmentCommand(command) } answers {}

            mockManager.addEquipmentCommand(command)

            verify { mockManager.addEquipmentCommand(command) }
        }
    })
